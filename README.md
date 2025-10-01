# Simulador del Sistema Solar

Aplicación JavaFX que simula el movimiento de los planetas del sistema solar alrededor del Sol aplicando la ley de gravitación universal de Newton. Permite modificar la fórmula usada para calcular la fuerza gravitatoria, editar los parámetros de cada planeta y observar sus órbitas en un entorno 3D interactivo.

## Requisitos

- Java 17 o superior
- Maven 3.8+
- JavaFX 20.0.1 (las dependencias incluidas están configuradas para Linux; ajuste el valor de `javafx.platform` en el `pom.xml` si ejecuta en otro sistema operativo)

## Ejecución

```bash
mvn clean javafx:run
```

## Características principales

- Barra superior con controles para pausar/continuar la simulación, modificar la velocidad del tiempo y abrir la configuración general.
- Ventana de configuración que permite editar la fórmula de gravedad (variables disponibles: `G`, `m1`, `m2`, `r`).
- Entorno tridimensional donde se puede rotar la cámara con el botón derecho del ratón y realizar zoom con la rueda.
- Selección de planetas para editar su nombre, masa y distancia al Sol (en kilómetros). Los cambios se aplican de inmediato a la simulación.
- Estelas que muestran la trayectoria de cada planeta a medida que orbitan.
