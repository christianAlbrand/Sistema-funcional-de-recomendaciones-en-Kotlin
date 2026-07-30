# Motor Funcional de Recomendaciones en Kotlin

Sistema de recomendaciones personalizadas para comercio electrónico desarrollado en Kotlin puro, diseñado para ejecutarse en Android Studio o cualquier entorno JVM.

---

## Requisitos del Sistema
* **Entorno de desarrollo:** Android Studio (Iguana, Jellyfish o superior) o IntelliJ IDEA.
* **Versión de Java:** JDK 11 o JDK 17.
* **Versión de Kotlin:** 1.9 o superior.

---

## 📁 Estructura del Proyecto

El código está organizado modularmente dentro del paquete principal:

```text
com.example.sistemafuncionalderecomendacionesenkotlin/
├── domain/            # Modelos inmutables (User, Product, Interaction)
├── result/            # Manejo monádico de errores (AppResult)
├── validation/        # Validaciones funcionales acumulativas
├── normalization/     # Pipeline de composición de funciones
├── scoring/           # Reglas explicativas de puntuación
├── profile/           # Cálculo funcional de perfiles de usuario
├── similarity/        # Coeficiente de Jaccard entre usuarios
├── recommendation/    # Motor de recomendaciones y acumulador fold
├── recursive/         # Algoritmo recursivo para taxonomías
├── reporting/         # Exportación de reportes en TXT y JSON
└── infrastructure/    # Generador de datos de prueba (100k interacciones)

🛠️ Instrucciones de Ejecución
Ejecutar el Proyecto Principal:
Abre el proyecto en Android Studio.

Espera a que termine la sincronización de Gradle.

Navega en el panel izquierdo a: src/main/kotlin/.../Main.kt.

Haz clic en el botón de Play (▶) al lado de la función fun main() y selecciona Run 'MainKt'.

Ejecutar las Pruebas Unitarias:
Navega en la carpeta de pruebas a: src/test/kotlin/.../ValidationAndInvariantsTest.kt.

Haz clic secundario sobre el archivo.

Selecciona la opción Run 'ValidationAndInvariantsTest'.

📊 Archivos Generados
Al finalizar la ejecución del archivo Main.kt, el sistema creará automáticamente dos archivos en la carpeta raíz de tu proyecto:

resultados_recomendaciones.txt: Contiene la lista detallada de productos sugeridos con sus precios, calificaciones y las razones de su puntuación.

reporte.json: Contiene un resumen estadístico con el total de productos evaluados, promedio de puntuación, desglose por categoría y motivos de rechazo.
