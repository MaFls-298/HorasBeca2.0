//variiables
    const usuario = JSON.parse(localStorage.getItem('usuario'))
    const encargadoId = usuario.id;
    document.getElementById("encargadoNombre").textContent = usuario.nombre;
    let actividadSeleccionada = null;

    // tabs
    function openTab(tabName) {
      const tabContents = document.getElementsByClassName("tab-content");
      for (let tab of tabContents) {
        tab.classList.remove("active");
      }

      const tabButtons = document.getElementsByClassName("tab-button");
      for (let button of tabButtons) {
        button.classList.remove("active");
      }

      document.getElementById(tabName).classList.add("active");
      event.currentTarget.classList.add("active");

      if (tabName === 'actividades') { //refresh
        listarActividades();
      }
    }

      //publicar actividad
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
              listarActividades(); // refresh
              document.getElementById("formNuevaActividad").reset();
          } else {
              alert("❌ Error al publicar: " + data.error);
          }
      } catch (error) {
          console.error("Fetch error:", error);
          alert("❌ Error de conexion");
      }
    }

    
    async function listarActividades() {
      try {
        const res = await fetch("/actividades");
        const actividades = await res.json();

        const tbody = document.getElementById("actividadesBody");
        tbody.innerHTML = "";

        const actividadesFiltradas = actividades.filter(a => a.encargadoId === encargadoId);
        
        if (actividadesFiltradas.length === 0) {
          tbody.innerHTML = `<tr><td colspan="6" style="text-align: center;">No hay actividades publicadas</td></tr>`;
          return;
        }

        actividadesFiltradas.forEach(actividad => {
          const fila = document.createElement("tr");
          fila.innerHTML = `
            <td>${actividad.titulo}</td>
            <td>${actividad.fechaActividad}</td>
            <td>${actividad.horaActividad}</td>
            <td>${actividad.cupoUsado}/${actividad.cupoMaximo}</td>
            <td>${actividad.cupoUsado < actividad.cupoMaximo ? '📝 Disponible' : '🔒 Lleno'}</td>
            <td>
              <button class="btn btn-primary" onclick="verDetalles(${actividad.id})">👁️ Ver</button>
              <button class="btn btn-warning" onclick="cargarParaEditar(${actividad.id})">✏️ Editar</button>
              <button class="btn btn-danger" onclick="confirmarEliminar(${actividad.id})">🗑️ Eliminar</button>
            </td>
          `;
          tbody.appendChild(fila);
        });
      } catch (error) {
        console.error("Error al cargar actividades:", error);
        alert("❌ Error al cargar las actividades");
      }
    }

        
    async function verDetalles(actividadId) {
      try {
        const res = await fetch(`/actividades/${actividadId}`);
        const actividad = await res.json();
        
        actividadSeleccionada = actividad;
        
        
        document.getElementById("detallesActividad").innerHTML = `
          <h3>${actividad.titulo}</h3>
          <p><strong>Descripción:</strong> ${actividad.descripcion}</p>
          <p><strong>Horas otorgadas:</strong> ${actividad.horasOtorgadas}</p>
          <p><strong>Cupo:</strong> ${actividad.cupoUsado}/${actividad.cupoMaximo}</p>
          <p><strong>Fecha:</strong> ${actividad.fechaActividad}</p>
          <p><strong>Hora:</strong> ${actividad.horaActividad}</p>
        `;
        
        
        document.getElementById("detallesActividadContainer").style.display = "block";
        document.getElementById("sinSeleccion").style.display = "none";
        
        
        await verAlumnos(actividadId);
        
        
        openTab('detalles');
      } catch (error) {
        console.error("Error al cargar detalles:", error);
        alert("❌ Error al cargar los detalles de la actividad");
      }
    }

    
    async function verAlumnos(actividadId) {
      try {
        const res = await fetch(`/actividades/${actividadId}/alumnos`);
        const alumnos = await res.json();

        const contenedor = document.getElementById("alumnosInscritos");
        
        if (alumnos.length === 0) {
          contenedor.innerHTML = `<h3>Alumnos Inscritos</h3><p>No hay alumnos inscritos en esta actividad.</p>`;
          return;
        }

        let tablaHTML = `
          <h3>Alumnos Inscritos (${alumnos.length})</h3>
          <table>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Email</th>
                <th>Horas Realizadas</th>
                <th>Asistencia</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
        `;

        alumnos.forEach(alumno => {
          tablaHTML += `
            <tr>
              <td>${alumno.nombre}</td>
              <td>${alumno.email}</td>
              <td>${alumno.horasRealizadas || 0}</td>
              <td>${alumno.asistenciaValidada ? "✅ Validada" : "❌ Pendiente"}</td>
              <td>
                ${!alumno.asistenciaValidada ? 
                  `<button class="btn btn-success" onclick="validarAsistencia(${actividadId}, ${alumno.id})">✅ Validar Asistencia</button>` : 
                  "✅ Completada"}
              </td>
            </tr>
          `;
        });

        tablaHTML += `</tbody></table>`;
        contenedor.innerHTML = tablaHTML;
      } catch (error) {
        console.error("Error al cargar alumnos:", error);
        document.getElementById("alumnosInscritos").innerHTML = `<h3>Alumnos Inscritos</h3><p>Error al cargar la lista de alumnos.</p>`;
      }
    }

    async function validarAsistencia(actividadId, alumnoId) {
      if (!confirm("¿Confirmar la asistencia de este alumno?")) return;
      
      try {
        const res = await fetch(`/actividades/${actividadId}/validar/${alumnoId}`, {
          method: "POST"
        });
        const data = await res.json();

        if (data.success) {
          alert("✅ Asistencia validada correctamente");
          await verAlumnos(actividadId);
        } else {
          alert("❌ Error al validar asistencia: " + data.error);
        }
      } catch (error) {
        console.error("Error al validar asistencia:", error);
        alert("❌ Error de conexión");
      }
    }


    async function cargarParaEditar(actividadId) {
      try {
        const res = await fetch(`/actividades/${actividadId}`);
        const actividad = await res.json();
        
        
        document.getElementById("editarId").value = actividad.id;
        document.getElementById("editarTitulo").value = actividad.titulo;
        document.getElementById("editarDescripcion").value = actividad.descripcion;
        document.getElementById("editarHoras").value = actividad.horasOtorgadas;
        document.getElementById("editarCupoMaximo").value = actividad.cupoMaximo;
        document.getElementById("editarFecha").value = actividad.fechaActividad;
        document.getElementById("editarHora").value = actividad.horaActividad;
        
        
        document.getElementById("modalEditar").style.display = "block";
      } catch (error) {
        console.error("Error al cargar actividad para editar:", error);
        alert("❌ Error al cargar la actividad");
      }
    }

    async function guardarEdicion() {
      const actividadId = document.getElementById("editarId").value;
      const actividadActualizada = {
        titulo: document.getElementById("editarTitulo").value,
        descripcion: document.getElementById("editarDescripcion").value,
        horasOtorgadas: parseInt(document.getElementById("editarHoras").value),
        cupoMaximo: parseInt(document.getElementById("editarCupoMaximo").value),
        fechaActividad: document.getElementById("editarFecha").value,
        horaActividad: document.getElementById("editarHora").value
      };

      try {
        const res = await fetch(`/actividades/${actividadId}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(actividadActualizada)
        });
        
        const data = await res.json();
        
        if (data.success) {
          alert("✅ Actividad actualizada correctamente");
          cerrarModal();
          listarActividades();
          
          if (actividadSeleccionada && actividadSeleccionada.id == actividadId) {
            await verDetalles(actividadId);
          }
        } else {
          alert("❌ Error al actualizar: " + data.error);
        }
      } catch (error) {
        console.error("Error al guardar cambios:", error);
        alert("❌ Error de conexión");
      }
    }

    function cerrarModal() {
      document.getElementById("modalEditar").style.display = "none";
    }

    async function confirmarEliminar(actividadId) {
      if (!confirm("¿Estás seguro de que quieres eliminar esta actividad? Esta acción no se puede deshacer.")) {
        return;
      }
      
      try {
        const res = await fetch(`/actividades/${actividadId}`, {
          method: "DELETE"
        });
        
        const data = await res.json();
        
        if (data.success) {
          alert("✅ Actividad eliminada correctamente");
          listarActividades();
          
          if (actividadSeleccionada && actividadSeleccionada.id == actividadId) {
            volverALista();
          }
        } else {
          alert("❌ Error al eliminar: " + data.error);
        }
      } catch (error) {
        console.error("Error al eliminar actividad:", error);
        alert("❌ Error de conexión");
      }
    }

    function volverALista() {
      openTab('actividades');
    }



    function logout() {
      localStorage.clear();
      window.location.href = '/index.html';
    }

    document.addEventListener("DOMContentLoaded", function() {
      listarActividades();
    });