DB
sqlite3 UsuariosDB.db
tabla: usuarios
SELECT * FROM usuarios;

→Terminal
javac -cp ".;lib/sqlite-jdbc-3.50.3.0.jar" -d . src/database/DbConnection.java src/ui/LoginUserView.java src/Main.java


→run
java -cp ".;lib/sqlite-jdbc-3.50.3.0.jar" Main
java -cp "bin;lib/sqlite-jdbc-3.50.3.0.jar" Main


conexion a sqlite y ui de login funcional


usuarios test guardados:
Encargado Ejemplo | mail@uvg | 6666 |
Alumno Ejemplo | mail@mail.edu | password |

**Subir todo a la branch "master" porque al crear otra rama subiendo archivos separados se crea conflicto de historiales** 


Hagan el resto ustedes. yo y mi chatgpt ya hicimos mucho 🤑
