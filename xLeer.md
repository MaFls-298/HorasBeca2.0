DB
sqlite3 UsuariosDB.db
tabla: usuarios
SELECT * FROM usuarios;


→Terminal
javac -cp "lib/*;src" -d bin src/server/WebServer.java src/database/DbConnection.java


→run
java -cp "lib/*;bin" server.WebServer
localhost:8080

conexion a sqlite y ui de login funcional. info de los usuarios 100% segura


usuarios test guardados:
Encargado Ejemplo | mail@uvg | 6666 |
Alumno Ejemplo | mail@mail.edu | password |

**Subir todo a la branch "master" porque al crear otra rama subiendo archivos separados se crea conflicto de historiales** 


Hagan el resto ustedes. yo y mi chatgpt ya hicimos mucho 🤑
