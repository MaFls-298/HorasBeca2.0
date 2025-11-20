//variiables
const usuario = JSON.parse(localStorage.getItem('usuario'))
const encargadoId = usuario.id;
document.getElementById("encargadoNombre").textContent = usuario.nombre;

let actividadSeleccionada = null;

document.addEventListener("DOMContentLoaded", () => {
    loadActividadesPublicadas();
});


// tabs
function openTab(tabName) {
    const tabContents = document.getElementsByClassName("tab-content");
    for (let tab of tabContents) {
        tab.classList.remove("active");
        tab.style.display = "none"; // hide all
    }

    const tabButtons = document.getElementsByClassName("tab-button");
    for (let button of tabButtons) {
        button.classList.remove("active");
    }

    // selected 
    const currentTab = document.getElementById(tabName);
    currentTab.classList.add("active");
    currentTab.style.display = "block";

    event.currentTarget.classList.add("active"); 

    if (tabName === 'misActividades') {
        loadActividadesPublicadas();
    }
}

//publicar actividad ///////////////////////////////////////////////////////////////////////

async function publicarActividad() {
  const fechaInput = document.getElementById("fecha").value; 
  const horaInput = document.getElementById("hora").value;

  const actividad = {
    titulo: document.getElementById("titulo").value,
    descripcion: document.getElementById("descripcion").value,
    horasOtorgadas: parseInt(document.getElementById("horas").value),
    cupoMaximo: parseInt(document.getElementById("cupoMaximo").value),
    cupoUsado: 0,
    fechaActividad: fechaInput,
    horaActividad: horaInput,
    encargadoId: encargadoId,
    actividadState: true
  };
    
    console.log("actvidad log:", actividad);

  try {
    const res = await fetch("/actividades", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(actividad)
    });

    console.log("Response status:", res.status); // log
          
    const data = await res.json();
    console.log("Response data:", data); // log

    if (data.success) {
      alert("✅ Actividad publicada correctamente");
      loadActividadesPublicadas();
      document.getElementById("formNuevaActividad").reset();
    } else {
      alert("❌ Error al publicar: " + data.error);
    }

  } catch (error) {
    console.error("Fetch error:", error);
    alert("❌ Error de conexion");
  }
}



// Activiidades publicadas por encargado //////////////////////////////////////////////////

async function loadActividadesPublicadas() {
  console.log("Encargado ID:", encargadoId);

    try {
        console.log("Fetching activities for encargado:", encargadoId);
        
        const response = await fetch(`http://localhost:8080/actividades/encargado/${encargadoId}`);
        if (!response.ok) {
            throw new Error("No se pudo obtener actividades");
        }

        const data = await response.json();
        console.log("Actividades found:", data);

        if (!Array.isArray(data.actividades)) {
            console.error("No se recibió un array de actividades:", data.actividades);
            return;
        }

        showCardsActividades(data.actividades);

    } catch (err) {
        console.error("Error cargando actividades:", err);
    }
}

function showCardsActividades(actividades) {
    const container = document.getElementById("cards");
    if (!container) return; 
    container.innerHTML = "";

    if (!actividades || actividades.length === 0) {
        container.innerHTML = "<p>No hay actividades disponibles.</p>";
        return;
    }

    actividades.forEach(act => {
      console.log("Actividad:", act)
        const card = document.createElement("div");
        card.classList.add("card");

        card.innerHTML = `
            <div class="status-circle ${act.actividadState ? 'disponible' : 'no-disponible'}" 
            onclick="toggleDisponibilidad(${act.id}, this)"></div>
            <h3>${act.titulo}</h3>
            <p><strong>Descripción:</strong> ${act.descripcion}</p>
            <p><strong>Fecha:</strong> ${act.fechaActividad}</p>
            <p><strong>Hora:</strong> ${act.horaActividad}</p>
            <p><strong>Horas Otorgadas:</strong> ${act.horasOtorgadas}</p>
            <p><strong>Cupo:</strong> ${act.cupoUsado}/${act.cupoMaximo}</p>
            <div class="buttons">
                <button onclick="verDetallesActividad(${act.id})">Ver detalles</button>
                <button class="btn-editar" onclick="editarActividad(${act.id})">Editar</button>
                
                <button class="btn-eliminar" onclick="eliminarActividadFront(${act.id})">Eliminar</button>
            </div>
        `;

        container.appendChild(card);
    });
}



//ver detalles actividad ////////////////////////////////////////////////////////////////


async function verDetallesActividad(id) {
  
    try {
        const res = await fetch(`http://localhost:8080/actividades/inscritos/${id}`);
        const data = await res.json();

        console.log("Inscritos:", data);

        const lista = data.inscritos.map(a => {
            const disabled = a.validado ? 'disabled' : '';
            const label = a.validado ? 'Validado' : 'Validar Horas';
            return `
                <li>
                    <strong>${a.nombre}</strong> (${a.carnet}) - ${a.email}
                    <button onclick="validarHoras(${a.carnet}, ${id}, ${a.inscripcionId})" ${disabled}>
                        ${label}
                    </button>
                </li>
            `;
        }).join("");

        const detallesContent = document.getElementById("detallesContenido");
        detallesContent.innerHTML = `
            <h2>Detalles de Actividad</h2>
            <p><strong>ID:</strong> ${id}</p>

            <h3>Alumnos inscritos</h3>
            <ul>
                ${lista.length > 0 ? lista : "<em>No hay alumnos inscritos</em>"}
            </ul>
        `;

        document.getElementById("detallesDetalles").style.display = "block";

    } catch (error) {
        console.error("Error cargando inscritos:", error);
    }
}

function cerrarDetalles() {
    document.getElementById("detallesDetalles").style.display = "none";
}

async function validarHoras(alumnoId, actividadId, inscripcionId) {
    if (!confirm("¿Seguro que deseas validar las horas de este alumno? Esta acción no se puede deshacer.")) {
        return;
    }

    try {
        const res = await fetch("http://localhost:8080/inscripcion/validarHoras", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ inscripcionId, actividadId })
        });

        const data = await res.json();
        if (data.success) {
            alert("Horas validadas correctamente");
            
            verDetallesActividad(actividadId); //refresh
        } else {
            alert("Error al validar horas: " + data.msg);
        }

    } catch (error) {
        console.error("Error validando horas:", error);
    }
}




//cambiar disponible ////////////////////////////////////////////////////////////////

async function toggleDisponibilidad(actividadId, circleElement) {
    try {
        const res = await fetch("http://localhost:8080/actividades/toggleDisponibilidad", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ actividadId })
        });

        const data = await res.json();

        if (data.success) {
            // Update circle color
            if (data.newState) {
                circleElement.classList.remove("no-disponible");
                circleElement.classList.add("disponible");
            } else {
                circleElement.classList.remove("disponible");
                circleElement.classList.add("no-disponible");
            }

        } else {
            alert("Error: " + data.msg);
        }

    } catch (err) {
        console.error("Error toggling disponibilidad:", err);
        alert("Error en el servidor");
    }
}




//eliminar actividad ////////////////////////////////////////////////////////////////

async function eliminarActividadFront(actividadId) {
    const confirmar = window.confirm("¿Estás seguro que quieres eliminar esta actividad?");
    if (!confirmar) return;

    try {
        const res = await fetch("http://localhost:8080/actividades/eliminar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ actividadId })
        });

        const data = await res.json();

        if (data.success) {
            alert(data.msg);
            loadActividadesPublicadas();
        } else {
            alert("No se pudo eliminar la actividad: " + data.msg);
        }
    } catch (error) {
        console.error("Error eliminando actividad:", error);
        alert("Error al eliminar la actividad.");
    }
}



//editar act //////////////////////////////////////////////////////////////

async function editarActividad(id) {
    try {
        const res = await fetch(`http://localhost:8080/actividades/${id}`);
        const data = await res.json();
        if (!data.success) {
            alert("No se pudo cargar la actividad");
            return;
        }

        const act = data.actividad;

        document.getElementById("editActividadId").value = act.id;
        document.getElementById("editTitulo").value = act.titulo;
        document.getElementById("editDescripcion").value = act.descripcion;
        document.getElementById("editHoras").value = act.horasOtorgadas;
        document.getElementById("editCupo").value = act.cupoMaximo;
        document.getElementById("editFecha").value = act.fechaActividad;
        document.getElementById("editHora").value = act.horaActividad || '';

        document.getElementById("modalEditarActividad").style.display = "flex";
    } catch (err) {
        console.error("Error cargando actividad:", err);
    }
}

function cerrarModalEditar() {
    document.getElementById("modalEditarActividad").style.display = "none";
}

// Submit 
document.getElementById("formEditarActividad").addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = document.getElementById("editActividadId").value;
    const titulo = document.getElementById("editTitulo").value;
    const descripcion = document.getElementById("editDescripcion").value;
    const horasOtorgadas = parseInt(document.getElementById("editHoras").value);
    const cupoMaximo = parseInt(document.getElementById("editCupo").value);
    const fechaActividad = document.getElementById("editFecha").value;
    const horaActividad = document.getElementById("editHora").value;

    try {
        const res = await fetch(`http://localhost:8080/actividades/editar/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ titulo, descripcion, horasOtorgadas, cupoMaximo, fechaActividad, horaActividad })
        });

        const data = await res.json();
        if (data.success) {
            alert("Actividad actualizada correctamente");
            cerrarModalEditar();
            loadActividadesPublicadas(); // refresh cards
        } else {
            alert("Error: " + data.msg);
        }
    } catch (err) {
        console.error("Error editando actividad:", err);
    }
});




function cerrarModalEditar() {
    document.getElementById("modalEditarActividad").style.display = "none";
}

document.getElementById("formEditarActividad").addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = document.getElementById("editActividadId").value;
    const titulo = document.getElementById("editTitulo").value;
    const descripcion = document.getElementById("editDescripcion").value;
    const horasOtorgadas = parseInt(document.getElementById("editHoras").value);
    const cupoMaximo = parseInt(document.getElementById("editCupo").value);
    const fechaActividad = document.getElementById("editFecha").value;
    const horaActividad = document.getElementById("editHora").value;

    try {
        const res = await fetch(`http://localhost:8080/actividades/editar/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ titulo, descripcion, horasOtorgadas, cupoMaximo, fechaActividad, horaActividad })
        });

        const data = await res.json();
        if (data.success) {
            alert("Actividad actualizada correctamente");
            cerrarModalEditar();
            loadActividadesPublicadas(); // refresh cards
        } else {
            alert("Error: " + data.msg);
        }

    } catch (err) {
        console.error("Error al actualizar actividad:", err);
    }
});





//logout ////////////////////////////////////////////////////////////////
function logout() {
  localStorage.clear();
  window.location.href = '/index.html';
}

