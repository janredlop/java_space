# Simulador del Sistema Solar

Esta aplicación implementa un simulador tridimensional del sistema solar escrito en Java y JavaFX. Permite observar el movimiento de los planetas alrededor del Sol basándose en la fórmula de la gravitación universal de Newton.

## Características principales

- Vista 3D interactiva con cámara orbitable (clic derecho y arrastrar) y zoom con la rueda del ratón.
- Barra superior con controles de reproducción (pausa/continuar), control de velocidad de la simulación y contador de tiempo simulado.
- Configuración dinámica de la fórmula gravitacional utilizada para calcular la aceleración de los planetas.
- Panel lateral con la información del planeta seleccionado (nombre, masa y distancia al Sol) editable en tiempo real.
- Estela visual para cada planeta que ayuda a interpretar su órbita.

## Requisitos

- JDK 17 o superior
- Maven 3.8+

> **Nota:** El `pom.xml` declara dependencias de JavaFX 17.0.9. Asegúrate de tener acceso a Maven Central para descargarlas al compilar o ejecutar.

## Compilación y ejecución

```bash
cd solar-simulator
mvn javafx:run
```

La aplicación se abrirá mostrando la vista tridimensional del sistema solar. Puedes:

- Hacer clic derecho y arrastrar para rotar la cámara.
- Usar la rueda del ratón para acercar o alejar la vista.
- Seleccionar un planeta con clic izquierdo para editar sus parámetros.
- Usar el botón de configuración para modificar la fórmula gravitatoria.

## Estructura del proyecto

```
solar-simulator/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/solarsimulator/
        │       ├── Planet.java
        │       ├── PlanetTrail.java
        │       └── SolarSystemSimulatorApp.java
        └── resources/
```
