# 🌿 TuPausa — Aplicación Móvil de Pausas Activas

> Aplicación móvil nativa para Android que promueve la práctica de pausas activas entre estudiantes y trabajadores con uso intensivo de pantallas.

**Universidad Distrital Francisco José de Caldas**  
Facultad Tecnológica — Tecnología en Sistematización de Datos  
Brayan Acosta Parrado & Jorge Daniel Callejas Cendales — 2026

---

## ⬇️ Descarga e Instalación

### 📱 Requisitos
- Android **8.0 (Oreo) o superior**
- Mínimo **50 MB** de espacio libre
- Conexión a internet para el registro inicial

### 🚀 Instalar TuPausa en 3 pasos

**Paso 1 — Descargar el APK**

Ve a la sección de [**Releases**](../../releases) y descarga el archivo `.apk` de la última versión.

[![Descargar APK](https://img.shields.io/badge/⬇️%20Descargar-TuPausa%20v1.0-darkred?style=for-the-badge)](../../releases/latest/download/TuPausa-v1.0.apk)

**Paso 2 — Habilitar fuentes desconocidas**

Como la app no se distribuye por Google Play, debes permitir la instalación manual:

1. Ve a **Ajustes → Seguridad** (o **Privacidad**)
2. Activa **"Instalar apps de fuentes desconocidas"**
3. En Android 8+, el sistema te lo pedirá automáticamente al abrir el APK

**Paso 3 — Instalar y abrir**

1. Abre el archivo `.apk` desde el administrador de archivos o notificaciones
2. Toca **Instalar** y espera a que finalice
3. Toca **Abrir** o búscala como **TuPausa** en tus aplicaciones

> ⚠️ Si tu dispositivo bloquea la instalación, verifica que hayas habilitado los permisos desde el mismo navegador o app que usaste para descargar el APK.

---

## 📋 Descripción del Proyecto

Los estudiantes de la Universidad Distrital enfrentan problemas de salud por el uso prolongado de dispositivos tecnológicos: fatiga ocular, dolor muscular, estrés y sedentarismo. **TuPausa** es una solución móvil que integra la gestión de pausas activas con una arquitectura de software de alto rendimiento para atacar directamente esta problemática.

---

## ✨ Características

| Funcionalidad | Descripción |
|---|---|
| 🔐 **Autenticación** | Registro e inicio de sesión con roles Estudiante / Administrador |
| ⏰ **Mis Rutinas** | Programa alarmas con días, hora y ejercicios personalizados |
| 🧘 **Pausas Activas** | Catálogo de ejercicios por categoría con guías visuales |
| 📊 **Mi Progreso** | Historial, estadísticas y gráfico de actividad semanal |
| 🔔 **Notificaciones** | Alertas persistentes para no omitir las pausas |
| ☁️ **Sincronización** | Datos respaldados en la nube mediante AWS |

---

## 🏗️ Arquitectura
```
TuPausa
├── Frontend Mobile        → Kotlin + Jetpack Compose (MVVM)
├── Base de datos local    → SQLite (Room)
├── Backend serverless     → AWS Lambda (Python)
├── Almacenamiento nube    → Amazon S3
└── API REST               → AWS API Gateway
```

### Patrón MVVM
```
Vista (Composables)
    ↕
ViewModel (lógica de presentación)
    ↕
Modelo (Repositorio → Room / Retrofit → AWS)
```

---

## 🗄️ Base de Datos

La app usa **SQLite** localmente y sincroniza con **Amazon S3** en la nube.
```sql
CREATE TABLE IF NOT EXISTS Usuarios (
    id_usuario         INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre             TEXT NOT NULL,
    correo_electronico TEXT UNIQUE NOT NULL,
    contrasena         TEXT NOT NULL,
    id_tipo_usuario    INTEGER,
    FOREIGN KEY (id_tipo_usuario) REFERENCES Tipo_usuario(id_tipo_usuario)
);
```

### Flujo de sincronización
```
App Android
    │
    ▼ HTTP (Retrofit)
API Gateway
    │
    ▼ Trigger
AWS Lambda (Python)
    ├── download_db()  ← descarga desde S3
    ├── operación SQL  ← INSERT / GET / PUT / DELETE
    └── upload_db()    → sube versión actualizada a S3
```

### Función Lambda — Gestión de la base de datos
```python
import boto3, sqlite3, os, json

BUCKET_NAME = 'proyectogradobucket'
DB_FILE     = '/tmp/Proyecto_grado.db'
s3          = boto3.client('s3')

def download_db():
    s3.download_file(BUCKET_NAME, 'Proyecto_grado.db', DB_FILE)

def upload_db():
    if os.path.exists(DB_FILE):
        s3.upload_file(DB_FILE, BUCKET_NAME, 'Proyecto_grado.db')

def lambda_handler(event, context):
    try:
        method = event['httpMethod']
        path   = event['path']
        body   = json.loads(event.get('body') or '{}')

        download_db()
        conn   = sqlite3.connect(DB_FILE)
        cursor = conn.cursor()

        if path == '/usuarios':
            if method == 'POST':
                cursor.execute(
                    'INSERT INTO Usuarios (nombre, correo_electronico, contrasena, id_tipo_usuario) VALUES (?,?,?,?)',
                    (body['nombre'], body['correo_electronico'], body['contrasena'], body['id_tipo_usuario'])
                )
                conn.commit()
                return {'statusCode': 201, 'body': json.dumps({'message': 'Usuario creado exitosamente'})}

            elif method == 'GET':
                cursor.execute('SELECT * FROM Usuarios')
                return {'statusCode': 200, 'body': json.dumps(cursor.fetchall())}

            elif method == 'PUT':
                cursor.execute(
                    'UPDATE Usuarios SET nombre=?, correo_electronico=?, contrasena=?, id_tipo_usuario=? WHERE id_usuario=?',
                    (body['nombre'], body['correo_electronico'], body['contrasena'], body['id_tipo_usuario'], body['id_usuario'])
                )
                conn.commit()
                return {'statusCode': 200, 'body': json.dumps({'message': 'Usuario actualizado exitosamente'})}

            elif method == 'DELETE':
                cursor.execute('DELETE FROM Usuarios WHERE id_usuario=?', (body['id_usuario'],))
                conn.commit()
                return {'statusCode': 200, 'body': json.dumps({'message': 'Usuario eliminado exitosamente'})}

        return {'statusCode': 404, 'body': json.dumps({'error': 'Endpoint not found'})}

    except Exception as e:
        return {'statusCode': 500, 'body': json.dumps({'error': str(e)})}
    finally:
        if 'conn' in locals():
            conn.close()
        upload_db()
```

---

## 📱 Integración con la App (Retrofit)
```kotlin
interface ApiService {
    @GET("usuarios")
    fun getUsuarios(): Call<List<Usuario>>

    @POST("usuarios")
    fun createUsuario(@Body usuario: Usuario): Call<Usuario>
}

// La URL base se carga desde local.properties — nunca en el código fuente
val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.API_BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje mobile | Kotlin |
| UI | Jetpack Compose + Material3 |
| Arquitectura | MVVM |
| Base de datos local | SQLite (Room) |
| Backend | AWS Lambda (Python) |
| Almacenamiento nube | Amazon S3 |
| API | AWS API Gateway + Retrofit |
| Metodología | Scrum (14 Sprints) |

---

## 👨‍💻 Instalación para Desarrolladores
```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/tupausa.git
cd tupausa
```
```bash
# 2. Crear local.properties en la raíz del proyecto (NO subir a git)
echo "API_BASE_URL=https://tu-endpoint.amazonaws.com/etapa/" >> local.properties
```
```bash
# 3. Abrir en Android Studio y ejecutar
# Asegúrate de tener Android Studio Hedgehog o superior
```

> 🔒 **Seguridad**: El endpoint de la API **nunca** debe incluirse en el código fuente. Usa `local.properties` (ya incluido en `.gitignore`) para gestionar credenciales de forma segura. Para obtener acceso al endpoint de producción, contacta al equipo de desarrollo.

---

## 📁 Estructura del Proyecto
```
tupausa/
├── app/
│   ├── src/main/
│   │   ├── java/com/tupausa/
│   │   │   ├── model/          # Entidades y repositorios
│   │   │   ├── viewmodel/      # ViewModels
│   │   │   ├── view/           # Composables (UI)
│   │   │   ├── network/        # Retrofit + ApiService
│   │   │   └── database/       # Room + SQLite
│   │   └── res/
├── lambda/
│   └── lambda_function.py      # Función AWS Lambda
├── local.properties            # ← NO subir a git
└── README.md
```

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">
  <sub>Hecho con ❤️ por Brayan Acosta Parrado & Jorge Daniel Callejas Cendales — Universidad Distrital 2026</sub>
</div>
