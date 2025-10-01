Crea una aplicacion en Java tal que sea un simulador avanzado del sistema solar, basado en la formula de Newton. La simulacion contemplara el movimiento de los planetas en un espacio tridimensional
Debe representar graficamente cada planeta moviendose alrededor del sol. 

El interfaz de usuario se dividirá en dos partes:
 1) Una barra superior con una botonera que contenga:
    1.1) botón de pause y botón de continuar la simulacion
    1.2) Se mostrará el tiempo transcurrido y botones para acelerar el paso del tiempo en la simulacion
    1.3) También habrá un boton de configuración general, en el que se mostrará la formula de Newton que calcula el movimiento de los planetas, pudiendo modifica la formula y tras modificarla la simulacion deberá tener en cuenta los cambios para adaptar el movimiento de los planetas.
 3) El resto de la ventana contendrá la simulación con los planetas orbitando alrededor del Sol. Si se hace clic con el boton derecho y se desplaza el ratón sin soltar la pulsacion permitirá rotar en 3d la camara dentro de la simulación. Con la rueda del ratón se aplicará zoom in o zoom out a la simulación.

Si se selecciona con el raton a un planeta, mostrará un panel con informacion sobre dicho planeta, indicando su nombre, masa y distancia al sol medida en kilómetros. 
Esta informacion podrá ser modificada por el usuario, y los valores se guardarán y serán tenidos en cuenta por la simulación, que cambiará el moviento del planeta y la distancia al sol, según los nuevos parametros.
Durante su movimiento cada planeta dejará una linea a modo de estela que permitirá hacerse una idea de su órbita.
