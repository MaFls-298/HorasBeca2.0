// variables
let alumnoId = null;
let usuario = null;
let actividadesDisponibles = [];


document.addEventListener('DOMContentLoaded', function() {
    usuario = JSON.parse(localStorage.getItem('usuario'));
    

    alumnoId = usuario.id;
    document.getElementById("alumnoNombre").textContent = usuario.nombre;
    
    // datos iniciales
    cargarHorasAlumno();
    cargarActividadesDisponibles();
    cargarHistorialAlumno();
    cargarDepartamentos();
});

// Tab s
function openTab(tabName) {
    // Hide
    const tabContents = document.getElementsByClassName("tab-content");
    for (let i = 0; i < tabContents.length; i++) {
        tabContents[i].classList.remove("active");
    }

    
    const tabButtons = document.getElementsByClassName("tab-button");
    for (let i = 0; i < tabButtons.length; i++) {
        tabButtons[i].classList.remove("active");
    }

    // cont tab especifics
    document.getElementById(tabName).classList.add("active");
    event.currentTarget.classList.add("active");
}

// cargar horas
async function cargarHorasAlumno() {
    try {
        const res = await fetch(`/alumnos/${alumnoId}/horas`);
        const data = await res.json();
        
        if (data.success) {
            document.getElementById('horasPendientes').textContent = data.horasPendientes;
            document.getElementById('horasAcumuladas').textContent = data.horasAcumuladas;
            document.getElementById('totalHoras').textContent = data.horasPendientes + data.horasAcumuladas;
            
            // progress
            const totalHoras = data.horasPendientes + data.horasAcumuladas;
            const porcentaje = totalHoras > 0 ? (data.horasAcumuladas / totalHoras) * 100 : 0;
            document.getElementById('progressFill').style.width = `${porcentaje}%`;
            document.getElementById('progressText').textContent = `${Math.round(porcentaje)}% completado`;
        }
    } catch (error) {
        console.error('Error loading hours:', error);
    }
}

// disponibles
async function cargarActividadesDisponibles() {
    try {
        const res = await fetch('/actividades/disponibles');
        const data = await res.json();
        
        if (data.success) {
            actividadesDisponibles = data.actividades;
            mostrarActividadesDisponibles(actividadesDisponibles);
        }
    } catch (error) {
        console.error('Error loading activities:', error);
    }
}

// Display 
function mostrarActividadesDisponibles(actividades) {
    const container = document.getElementById('listaActividades');
    
    if (actividades.length === 0) {
        container.innerHTML = '<p class="no-data">No hay actividades disponibles en este momento.</p>';
        return;
    }

    container.innerHTML = actividades.map(actividad => `
        <div class="activity-card" data-department="${actividad.departamento}">
            <h3>${actividad.titulo}</h3>
            <p class="activity-description">${actividad.descripcion}</p>
            <div class="activity-details">
                <span class="detail"><strong>Horas:</strong> ${actividad.horasOtorgadas}</span>
                <span class="detail"><strong>Cupo:</strong> ${actividad.cupoUsado}/${actividad.cupoMaximo}</span>
                <span class="detail"><strong>Fecha:</strong> ${new Date(actividad.fechaActividad).toLocaleDateString()}</span>
                <span class="detail"><strong>Hora:</strong> ${actividad.horaActividad}</span>
                <span class="detail"><strong>Departamento:</strong> ${actividad.departamento}</span>
            </div>
            <button 
                class="enroll-btn ${actividad.cupoUsado >= actividad.cupoMaximo ? 'full' : ''}" 
                onclick="inscribirEnActividad(${actividad.id})"
                ${actividad.cupoUsado >= actividad.cupoMaximo ? 'disabled' : ''}
            >
                ${actividad.cupoUsado >= actividad.cupoMaximo ? 'Cupo Lleno' : 'Inscribirse'}
            </button>
        </div>
    `).join('');
}

// Filter
function filtrarActividades() {
    const searchTerm = document.getElementById('searchActivity').value.toLowerCase();
    const departmentFilter = document.getElementById('filterDepartment').value;
    
    const filtered = actividadesDisponibles.filter(actividad => {
        const matchesSearch = actividad.titulo.toLowerCase().includes(searchTerm) || 
                            actividad.descripcion.toLowerCase().includes(searchTerm);
        const matchesDepartment = !departmentFilter || actividad.departamento === departmentFilter;
        
        return matchesSearch && matchesDepartment;
    });
    
    mostrarActividadesDisponibles(filtered);
}

// departamento
async function cargarDepartamentos() {
    try {
        const res = await fetch('/departamentos');
        const data = await res.json();
        
        if (data.success) {
            const select = document.getElementById('filterDepartment');
            select.innerHTML = '<option value="">Todos los departamentos</option>' +
                data.departamentos.map(dept => `<option value="${dept}">${dept}</option>`).join('');
        }
    } catch (error) {
        console.error('Error loading departments:', error);
    }
}

// inscribir
async function inscribirEnActividad(actividadId) {
    if (!confirm('¿Estás seguro de que quieres inscribirte en esta actividad?')) {
        return;
    }

    try {
        const res = await fetch('/inscripciones', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                actividadId: actividadId,
                alumnoId: alumnoId
            })
        });

        const data = await res.json();
        
        if (data.success) {
            alert('✅ Te has inscrito correctamente en la actividad');
            cargarActividadesDisponibles(); // Refresh 
            cargarHistorialAlumno(); // Refresh 
        } else {
            alert('❌ Error: ' + data.error);
        }
    } catch (error) {
        console.error('Error enrolling:', error);
        alert('❌ Error de conexión');
    }
}


async function cargarHistorialAlumno() {
    try {
        const res = await fetch(`/alumnos/${alumnoId}/historial`);
        const data = await res.json();
        
        if (data.success) {
            mostrarHistorialAlumno(data.inscripciones);
        }
    } catch (error) {
        console.error('Error loading history:', error);
    }
}


function mostrarHistorialAlumno(inscripciones) {
    const tbody = document.querySelector('#tablaHistorial tbody');
    
    if (inscripciones.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data">No tienes actividades en tu historial.</td></tr>';
        return;
    }

    tbody.innerHTML = inscripciones.map(insc => `
        <tr data-status="${insc.asistenciaValidada ? 'validated' : insc.actividadCompletada ? 'completed' : 'pending'}">
            <td>${insc.tituloActividad}</td>
            <td>${new Date(insc.fechaActividad).toLocaleDateString()}</td>
            <td>${insc.horasOtorgadas}</td>
            <td>
                <span class="status-badge ${insc.asistenciaValidada ? 'validated' : insc.actividadCompletada ? 'completed' : 'pending'}">
                    ${insc.asistenciaValidada ? 'Validado' : insc.actividadCompletada ? 'Completado' : 'Pendiente'}
                </span>
            </td>
            <td>${new Date(insc.fechaInscripcion).toLocaleDateString()}</td>
        </tr>
    `).join('');
}

function filtrarHistorial() {
    const statusFilter = document.getElementById('filterStatus').value;
    const rows = document.querySelectorAll('#tablaHistorial tbody tr');
    
    rows.forEach(row => {
        if (!statusFilter || row.getAttribute('data-status') === statusFilter) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Logout
function cerrarSesion() {
    localStorage.removeItem('usuario');
    window.location.href = "/index.html";
}