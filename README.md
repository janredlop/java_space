# Simulador del Sistema Solar

Este proyecto contiene una aplicación JavaFX que simula el movimiento de los planetas alrededor del Sol utilizando la fórmula de gravitación universal de Newton.

## Funcionalidades principales

- Animación 3D del Sistema Solar con cámara orbitable (arrastre con botón derecho) y zoom con la rueda del ratón.
- Barra superior con botones de pausa/continuación, control de la velocidad temporal y acceso a la configuración.
- Editor de la fórmula de Newton: permite modificar la expresión utilizada para calcular la fuerza gravitatoria.
- Panel lateral con información del planeta seleccionado (nombre, masa y distancia al Sol) editable en tiempo real.
- Estelas de movimiento que ayudan a visualizar la órbita recorrida por cada planeta.

## Requisitos

- Java 17 o superior
- Maven 3.8+

## Ejecución

Para compilar y ejecutar la aplicación:

```bash
mvn clean javafx:run
```

La primera ejecución descargará las dependencias (JavaFX y el motor de expresiones `exp4j`).

## Notas

- La distancia al Sol se gestiona en kilómetros. La constante gravitatoria `G` por defecto está adaptada a dichas unidades.
- La fórmula editable debe utilizar las variables `G`, `m1`, `m2` y `r` (distancia en kilómetros) y puede emplear funciones estándar como `pow`, `sin`, `cos`, etc.
- Los cambios realizados sobre la fórmula o sobre los datos de un planeta se reflejan inmediatamente en la simulación.
