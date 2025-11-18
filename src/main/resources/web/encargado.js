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
            <h3>${act.titulo}</h3>
            <p><strong>Descripción:</strong> ${act.descripcion}</p>
            <p><strong>Fecha:</strong> ${act.fechaActividad}</p>
            <p><strong>Hora:</strong> ${act.horaActividad}</p>
            <p><strong>Cupo:</strong> ${act.cupoUsado}/${act.cupoMaximo}</p>
            <div class="buttons">
                <button onclick="verDetallesActividad(${act.id})">Ver detalles</button>
                <button class="btn-editar">Editar</button>
                <button class="btn-toggle" onclick="cambiarDisponibilidad(${act.id})"> ${act.actividadState ? 'Marcar como no disponible' : 'Marcar como disponible'} </button>
                <button class="btn-eliminar">Eliminar</button>
            </div>
        `;

        container.appendChild(card);
        console.log("Button HTML:", `
    <button onclick="verDetallesActividad(${act.id})">Ver detalles</button>
`);
    });
}

async function verDetallesActividad(id) {
  
    try {
        const res = await fetch(`http://localhost:8080/actividades/inscritos/${id}`);
        const data = await res.json();

        console.log("Inscritos:", data);

        const lista = data.inscritos.map(a =>
          `<li>
              <strong>${a.nombre}</strong> (${a.carnet}) - ${a.email}
              <button onclick="validarHoras(${a.carnet}, ${id}, ${a.inscripcionId})">
                  Validar Horas
              </button>
          </li>`
        ).join("");

        const modalContent = document.getElementById("modalContenido");
        modalContent.innerHTML = `
            <h2>Detalles de Actividad</h2>
            <p><strong>ID:</strong> ${id}</p>

            <h3>Alumnos inscritos</h3>
            <ul>
                ${lista.length > 0 ? lista : "<em>No hay alumnos inscritos</em>"}
            </ul>
        `;

        document.getElementById("modalDetalles").style.display = "block";

    } catch (error) {
        console.error("Error cargando inscritos:", error);
    }
}

function cerrarModal() {
    document.getElementById("modalDetalles").style.display = "none";
}

async function validarHoras(alumnoId, actividadId, inscripcionId) {
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
async function cambiarDisponibilidad(actividadId) {
    try {
        const res = await fetch("http://localhost:8080/actividades/toggleDisponibilidad", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ actividadId })
        });

        const data = await res.json();

        if (data.success) {
            alert(data.msg);
            // Refresh
            loadActividadesPublicadas();
        } else {
            alert("No se pudo cambiar disponibilidad: " + data.msg);
        }

    } catch (error) {
        console.error("Error cambiando disponibilidad:", error);
        alert("Error al cambiar disponibilidad.");
    }
}






function logout() {
  localStorage.clear();
  window.location.href = '/index.html';
}

