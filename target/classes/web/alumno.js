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
    loadActividadesDisponibles();
    cargarHistorialAlumno();
    cargarDepartamentos();
});

// Tabs//////////////////////////////////////////////////////////////

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

    // cont tab especificas
    document.getElementById(tabName).classList.add("active");
    event.currentTarget.classList.add("active");
}

// cargar horas ///////////////////////////////////////////////////

async function cargarHorasAlumno() {
    

    const usuario = JSON.parse(localStorage.getItem('usuario'));

    if (!usuario || !usuario.id) {
    console.error("No se encontro el ID del alumno en localStorage:", usuario);
    alert("Error: No se encontró informacion del alumno.");
    window.location.href = "index.html";
    return;
    }

    const alumnoId = usuario.id;
    console.log("Fetching hours for alumnoId:", alumnoId);

    try {
        const res = await fetch(`/alumnos/${alumnoId}/horas`);
        if (!res.ok) {
            const text = await res.text();
            console.error("Error server:", text);
            throw new Error(`HTTP ${res.status}`);
        }
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
        console.error('Error cargando horas:', error);
    }
}

// actividades disponibles //////////////////////////////////////////////

async function loadActividadesDisponibles() {
    try {
        const res = await fetch('/actividades/disponibles');
        const data = await res.json();
        console.log("Data from server:", data);

        if (!data.success) {
            console.error('Error:', data.error);
            return;
        }

        actividadesDisponibles = data.actividades;
        showActividades(actividadesDisponibles);
        cargarDepartamentos();


    } catch (error) {
        console.error('Error cargando actividades:', error);
    }
}

//tabla mostrar
function showActividades(actividades){
    console.log("Ejemplo de actividad:", actividades[0]);
        const tabla = document.getElementById('tablaActividades');
        tabla.innerHTML = ''; // clear 

        actividades.forEach(act => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${act.titulo}</td>
            <td>${act.descripcion}</td>
            <td>${act.horasOtorgadas}</td>
            <td>${act.cupoUsado}/${act.cupoMaximo}</td>
            <td>${act.fechaActividad}</td>
            <td>${act.horaActividad || ''}</td>
            <td><button onclick="inscribirse(${act.id})">Inscribirse</button></td>`;
            tabla.appendChild(row);
        });
}

// filtrar 
function filtrarActividades() {
    const searchTerm = document.getElementById('searchActivity').value.toLowerCase();
    const departmentFilter = document.getElementById('filterDepartment').value;

    console.log("Filtering-Search:", searchTerm, "Departamento:", departmentFilter);
    console.log("All actividades:", actividadesDisponibles);
    
    const filtered = actividadesDisponibles.filter(actividad => {
        console.log("Actividad departamento:", actividad.departamento, "Filter:", departmentFilter);
        const matchesSearch = actividad.titulo.toLowerCase().includes(searchTerm) || 
                            actividad.descripcion.toLowerCase().includes(searchTerm);
        
        const actividadDept = actividad.departamento ? actividad.departamento.toString().trim().toLowerCase() : "";
        const filterDept = departmentFilter ? departmentFilter.trim().toLowerCase() : "";
        
        const matchesDepartment = !departmentFilter || actividadDept === filterDept;
        
        console.log("Matches search:", matchesSearch, "Matches department:", matchesDepartment);
        
        return matchesSearch && matchesDepartment;
    });
    
    console.log("Filtered results:", filtered);
    showActividades(filtered);
}

// departamento /////////////////////////////////////////////////
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



// inscribirse //////////////////////////////////////////////////////
async function inscribirse(actividadId) {
    const usuario = JSON.parse(localStorage.getItem('usuario'));
    const alumnoId = usuario.id;

    // ✅ Confirmation message
    const confirmar = window.confirm("¿Estás seguro que quieres inscribirte en esta actividad?");
    if (!confirmar) return; // User canceled

    try {
        const res = await fetch("http://localhost:8080/actividades/inscribirse", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ alumnoId, actividadId })
        });

        const data = await res.json();

        if (data.success) {
            alert(data.msg);
            // Refresh the table to show updated cupoUsado
            loadActividadesDisponibles();
        } else {
            alert("No se pudo inscribir: " + data.msg);
        }

    } catch (error) {
        console.error("Error inscribiéndose:", error);
        alert("Error al inscribirse. Intenta nuevamente.");
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
function logout() {
    localStorage.removeItem('usuario');
    window.location.href = "/index.html";
}