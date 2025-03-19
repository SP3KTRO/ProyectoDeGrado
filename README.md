# Proyecto de Grado: Aplicación Móvil "TuPausa"

## Descripción del Proyecto

El proyecto "TuPausa" es una aplicación móvil desarrollada para los estudiantes del programa de Tecnología en Sistematización de Datos de la Universidad Distrital Francisco José de Caldas. La aplicación tiene como objetivo principal promover la práctica de pausas activas entre los estudiantes, con el fin de mejorar su salud física y mental, así como su bienestar integral y productividad.

### Problema Identificado

Los estudiantes de la Universidad Distrital enfrentan problemas de salud debido al uso prolongado de dispositivos tecnológicos, lo que genera fatiga ocular, dolor muscular, estrés y sedentarismo. A pesar de que las pausas activas son una solución recomendada, su implementación en el entorno académico es limitada y poco efectiva.

### Solución Tecnológica

La aplicación "TuPausa" se desarrolló en **Kotlin** utilizando **Jetpack Compose** para la interfaz de usuario y sigue la arquitectura **MVVM (Modelo-Vista-VistaModelo)**. La aplicación incluye las siguientes características clave:

1. **Autenticación y Gestión de Usuarios**: Los usuarios pueden registrarse y autenticarse, y se clasifican en dos tipos: "Estudiante" y "Administrador".
2. **Agendamiento de Pausas Activas**: Los usuarios pueden programar pausas activas con fecha, hora, duración y ejercicios específicos.
3. **Detección de Movimientos**: Utiliza tecnología de reconocimiento de poses (PoseNet) para verificar si los ejercicios se realizan correctamente.
4. **Recordatorios y Notificaciones**: Notificaciones configurables para recordar a los usuarios sus pausas programadas.
5. **Registro y Estadísticas**: Almacena un historial de pausas activas y genera estadísticas para evaluar el impacto en la salud y el bienestar.

---

## Arquitectura del Proyecto

### Base de Datos Local (SQLite)

La aplicación utiliza una base de datos local **SQLite** para almacenar los datos de los usuarios, pausas activas, ejercicios y estadísticas. La estructura de la base de datos sigue un diseño relacional con tablas como `Usuarios`, `Pausas_activas`, `Ejercicios`, `Registro_pausa`, entre otras.

#### Ejemplo de Creación de Tablas en SQLite:

```sql
CREATE TABLE IF NOT EXISTS Usuarios (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    correo_electronico TEXT UNIQUE NOT NULL,
    contrasena TEXT NOT NULL,
    id_tipo_usuario INTEGER,
    FOREIGN KEY (id_tipo_usuario) REFERENCES Tipo_usuario(id_tipo_usuario)
);
```

### Almacenamiento en Amazon S3

Para garantizar que los datos estén disponibles al instalar la aplicación, se utilizó un **bucket de Amazon S3** para almacenar la base de datos SQLite. La aplicación descarga la base de datos desde S3 al iniciar por primera vez, lo que permite que los datos estén disponibles sin necesidad de una conexión constante a Internet.

#### Código de la Función Lambda para Descargar/Subir la Base de Datos:

```python
import boto3
import sqlite3
import os

# Configuración de S3
BUCKET_NAME = 'proyectogradobucket'
DB_FILE = '/tmp/Proyecto_grado.db'

# Cliente de S3
s3 = boto3.client('s3')

def download_db():
    """Descarga la base de datos desde S3."""
    try:
        s3.download_file(BUCKET_NAME, 'Proyecto_grado.db', DB_FILE)
        print("Base de datos descargada correctamente.")
    except Exception as e:
        print(f"Error al descargar la base de datos: {str(e)}")
        raise e

def upload_db():
    """Sube la base de datos actualizada a S3."""
    try:
        if os.path.exists(DB_FILE):
            s3.upload_file(DB_FILE, BUCKET_NAME, 'Proyecto_grado.db')
            print("Base de datos subida correctamente.")
        else:
            print("El archivo de la base de datos no existe en /tmp.")
    except Exception as e:
        print(f"Error al subir la base de datos: {str(e)}")
        raise e
```

### API REST con AWS Lambda

La API REST se implementó utilizando **AWS Lambda** y **API Gateway**. La función Lambda maneja las solicitudes HTTP (GET, POST, PUT, DELETE) para interactuar con la base de datos SQLite almacenada en S3.

#### Ejemplo de Manejo de Solicitudes en Lambda:

```python
def lambda_handler(event, context):
    """Maneja las solicitudes a la función Lambda."""
    try:
        http_method = event['httpMethod']
        path = event['path']
        body = event.get('body', {})

        # Descargar la base de datos desde S3
        download_db()

        # Conectar a la base de datos SQLite
        conn = sqlite3.connect(DB_FILE)
        cursor = conn.cursor()

        if path == '/usuarios':
            if http_method == 'POST':
                # Insertar un nuevo usuario
                query = '''
                INSERT INTO Usuarios (nombre, correo_electronico, contrasena, id_tipo_usuario)
                VALUES (?, ?, ?, ?)
                '''
                cursor.execute(query, (body['nombre'], body['correo_electronico'], body['contrasena'], body['id_tipo_usuario']))
                conn.commit()
                return {
                    'statusCode': 201,
                    'body': json.dumps({'message': 'Usuario creado exitosamente'})
                }
            elif http_method == 'GET':
                # Obtener todos los usuarios
                query = 'SELECT * FROM Usuarios'
                cursor.execute(query)
                usuarios = cursor.fetchall()
                return {
                    'statusCode': 200,
                    'body': json.dumps(usuarios)
                }
    except Exception as e:
        print(f"Error en la función Lambda: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }
    finally:
        # Cerrar la conexión y subir la base de datos actualizada a S3
        if 'conn' in locals():
            conn.close()
        upload_db()
```

### URL de la API

La API REST está disponible en la siguiente URL:  
**https://5az7zcnh4j.execute-api.sa-east-1.amazonaws.com/Inicio/usuarios**

---

## Tecnologías Utilizadas

- **Lenguajes de Programación**: Kotlin, Python (para Lambda).
- **Frameworks y Librerías**: Jetpack Compose, Retrofit (para consumir la API).
- **Base de Datos**: SQLite.
- **Cloud**: Amazon S3 (almacenamiento), AWS Lambda (backend), API Gateway (API REST).
- **Arquitectura**: MVVM (Modelo-Vista-VistaModelo).

---

## Instalación y Uso

1. **Clonar el Repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/tupausa.git
   cd tupausa
   ```

2. **Configurar AWS**:
   - Crear un bucket en S3 y subir la base de datos `Proyecto_grado.db`.
   - Configurar la función Lambda y API Gateway en AWS.

3. **Ejecutar la Aplicación**:
   - Abrir el proyecto en Android Studio.
   - Configurar la URL de la API en el archivo `RetrofitClient.kt`.
   - Ejecutar la aplicación en un dispositivo Android o emulador.

---

## 1. **Almacenamiento en Amazon S3**

Amazon S3 es un servicio de almacenamiento en la nube que se utiliza para guardar la base de datos SQLite (`Proyecto_grado.db`) de la aplicación. Esto permite que la base de datos esté disponible para su descarga cuando un usuario instala la aplicación por primera vez.

### ¿Por qué usar S3?
- **Centralización**: La base de datos se almacena en un solo lugar (el bucket de S3), lo que facilita su gestión y actualización.
- **Disponibilidad**: Los usuarios pueden descargar la base de datos al instalar la aplicación, incluso si no tienen una conexión constante a Internet.
- **Escalabilidad**: S3 es altamente escalable y puede manejar grandes volúmenes de datos.

---

## 2. **Función Lambda para Descargar/Subir la Base de Datos**

La función Lambda en AWS actúa como un intermediario entre la aplicación móvil y el bucket de S3. Su propósito es:
- **Descargar la base de datos** desde S3 cuando la aplicación se inicia por primera vez.
- **Subir la base de datos actualizada** a S3 cuando se realizan cambios (por ejemplo, cuando un usuario realiza una pausa activa).

### Código de la Función Lambda

```python
import boto3
import sqlite3
import os

# Configuración de S3
BUCKET_NAME = 'proyectogradobucket'  # Nombre del bucket
DB_FILE = '/tmp/Proyecto_grado.db'   # Ruta temporal para el archivo SQLite

# Cliente de S3
s3 = boto3.client('s3')

def download_db():
    """Descarga la base de datos desde S3."""
    try:
        s3.download_file(BUCKET_NAME, 'Proyecto_grado.db', DB_FILE)
        print("Base de datos descargada correctamente.")
    except Exception as e:
        print(f"Error al descargar la base de datos: {str(e)}")
        raise e

def upload_db():
    """Sube la base de datos actualizada a S3."""
    try:
        if os.path.exists(DB_FILE):
            s3.upload_file(DB_FILE, BUCKET_NAME, 'Proyecto_grado.db')
            print("Base de datos subida correctamente.")
        else:
            print("El archivo de la base de datos no existe en /tmp.")
    except Exception as e:
        print(f"Error al subir la base de datos: {str(e)}")
        raise e
```

### ¿Cómo funciona?
1. **Descarga de la Base de Datos**:
   - Cuando la aplicación se inicia por primera vez, llama a la función Lambda para descargar la base de datos desde S3.
   - La función `download_db()` descarga el archivo `Proyecto_grado.db` y lo guarda en la ruta temporal `/tmp`.

2. **Subida de la Base de Datos**:
   - Cuando la aplicación realiza cambios en la base de datos (por ejemplo, insertar un nuevo usuario o registrar una pausa activa), llama a la función Lambda para subir la base de datos actualizada a S3.
   - La función `upload_db()` sube el archivo `Proyecto_grado.db` desde `/tmp` al bucket de S3.

---

## 3. **API REST con AWS Lambda y API Gateway**

La API REST se utiliza para que la aplicación móvil interactúe con la base de datos SQLite almacenada en S3. La función Lambda maneja las solicitudes HTTP (GET, POST, PUT, DELETE) y realiza operaciones en la base de datos.

### Código de la Función Lambda para la API REST

```python
def lambda_handler(event, context):
    """Maneja las solicitudes a la función Lambda."""
    try:
        http_method = event['httpMethod']
        path = event['path']
        body = event.get('body', {})

        # Descargar la base de datos desde S3
        download_db()

        # Conectar a la base de datos SQLite
        conn = sqlite3.connect(DB_FILE)
        cursor = conn.cursor()

        if path == '/usuarios':
            if http_method == 'POST':
                # Insertar un nuevo usuario
                query = '''
                INSERT INTO Usuarios (nombre, correo_electronico, contrasena, id_tipo_usuario)
                VALUES (?, ?, ?, ?)
                '''
                cursor.execute(query, (body['nombre'], body['correo_electronico'], body['contrasena'], body['id_tipo_usuario']))
                conn.commit()
                return {
                    'statusCode': 201,
                    'body': json.dumps({'message': 'Usuario creado exitosamente'})
                }
            elif http_method == 'GET':
                # Obtener todos los usuarios
                query = 'SELECT * FROM Usuarios'
                cursor.execute(query)
                usuarios = cursor.fetchall()
                return {
                    'statusCode': 200,
                    'body': json.dumps(usuarios)
                }
    except Exception as e:
        print(f"Error en la función Lambda: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }
    finally:
        # Cerrar la conexión y subir la base de datos actualizada a S3
        if 'conn' in locals():
            conn.close()
        upload_db()
```

### ¿Cómo funciona?
1. **Descarga de la Base de Datos**:
   - Antes de procesar cualquier solicitud, la función Lambda descarga la base de datos desde S3 utilizando `download_db()`.

2. **Operaciones en la Base de Datos**:
   - Dependiendo de la ruta (`path`) y el método HTTP (`httpMethod`), la función Lambda realiza operaciones en la base de datos SQLite.
   - Por ejemplo, si la ruta es `/usuarios` y el método es `POST`, se inserta un nuevo usuario en la tabla `Usuarios`.

3. **Subida de la Base de Datos**:
   - Después de realizar las operaciones, la función Lambda sube la base de datos actualizada a S3 utilizando `upload_db()`.

---

## 4. **Integración con la Aplicación Móvil**

La aplicación móvil se comunica con la API REST para realizar operaciones en la base de datos. Utiliza **Retrofit** para consumir la API y manejar las respuestas.

### Ejemplo de Uso de Retrofit en la Aplicación Móvil

```kotlin
interface ApiService {
    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @POST("usuarios")
    fun createUsuario(@Body usuario: Usuario): Call<Usuario>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://5az7zcnh4j.execute-api.sa-east-1.amazonaws.com/Inicio/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)

// Obtener usuarios desde la API
apiService.getUsuarios().enqueue(object : Callback<List<Usuario>> {
    override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
        if (response.isSuccessful) {
            val usuarios = response.body()
            // Procesar la lista de usuarios
        }
    }

    override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
        // Manejar el error
    }
})
```

### ¿Cómo funciona?
1. **Llamadas a la API**:
   - La aplicación móvil realiza llamadas a la API REST utilizando Retrofit.
   - Por ejemplo, para obtener la lista de usuarios, se hace una solicitud GET a la ruta `/usuarios`.

2. **Actualización de la Base de Datos**:
   - Cuando la aplicación realiza cambios (por ejemplo, insertar un nuevo usuario), llama a la API REST, que a su vez actualiza la base de datos en S3 a través de la función Lambda.

---

## 5. **Flujo de Datos Completo**

1. **Primer Inicio de la Aplicación**:
   - La aplicación descarga la base de datos desde S3 utilizando la función Lambda.
   - La base de datos se almacena localmente en el dispositivo.

2. **Operaciones en la Aplicación**:
   - La aplicación realiza operaciones (insertar, actualizar, eliminar) a través de la API REST.
   - La función Lambda descarga la base de datos desde S3, realiza las operaciones y sube la base de datos actualizada.

3. **Sincronización de Datos**:
   - Cada vez que se realiza una operación, la base de datos se actualiza en S3, lo que garantiza que todos los dispositivos tengan acceso a los datos más recientes.

---

## Licencia

Este proyecto está bajo la licencia MIT. Para más detalles, consulta el archivo [LICENSE](LICENSE).
