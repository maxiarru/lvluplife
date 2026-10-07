# PRD-001: Lvl up Life — una forma de mantener tu rutina activa

## Contexto y Problema

Martín tiene 34 años, trabaja, entrena cuando puede y quiere encontrar tiempo para leer, descansar mejor y comer de manera más saludable. Aunque durante la semana hace varias de esas cosas, al finalizar el día suele sentir que no avanzó porque sus logros cotidianos quedan dispersos y no puede verlos como parte de un progreso común. Probó aplicaciones de hábitos, pero las abandonó cuando registrar cada acción empezó a sentirse como una tarea más.

Sofía tiene 23 años, estudia y disfruta de los videojuegos. Quiere construir una rutina más equilibrada, pero las listas y calendarios no la motivan. Le resulta más atractivo recibir feedback inmediato, desarrollar un personaje y comparar su constancia con la de sus amigos, sin exponer públicamente los detalles de su vida.

Lvl Up Life transforma las actividades que una persona ya realizó —entrenar, moverse, alimentarse bien, leer, disfrutar de un pasatiempo o descansar— en el progreso de un personaje RPG. Los registros pueden aportar experiencia a uno o dos atributos, según los topes diarios y de nivel, y ese progreso permite subir niveles y modificar la clase y la apariencia del personaje. La aplicación busca que el usuario reconozca sus avances y vuelva voluntariamente, sin convertir el seguimiento en otra obligación.

La primera versión está dirigida a jóvenes y adultos y se desarrolla para Android. Registra actividades realizadas; no funciona como agenda de tareas futuras ni pretende contabilizar todo el tiempo del día.

## Objetivos

- Lograr que el usuario se sienta motivado a registrar sus actividades y pueda reconocer sus avances cotidianos mediante el progreso de su personaje.
- Generar una sensación clara e inmediata de progreso mediante mecanismos de gamificación que ayuden al usuario a sostener una rutina activa.

## Requerimientos Funcionales

- RF-01: El sistema debe permitir registrarse mediante una cuenta de Google, sin administrar una contraseña propia de LUL.
- RF-02: El sistema debe permitir iniciar sesión mediante una cuenta de Google ya registrada, sin solicitar una contraseña propia de LUL.
- RF-03: El sistema debe mantener una única cuenta por identidad de Google.
- RF-04: El sistema debe permitir elegir un nombre de usuario único.
- RF-05: El sistema debe crear exactamente un personaje por cuenta, inicialmente en nivel general 1, con los seis atributos en nivel 1, 0 XP y clase Novato.
- RF-06: El sistema debe ofrecer un catálogo inicial de actividades básicas con modalidad, categoría y atributos preasignados.
- RF-07: El sistema debe permitir crear una actividad personalizada especificando nombre, categoría, atributo principal y, opcionalmente, un atributo secundario compatible.
- RF-08: El sistema debe guardar la actividad personalizada cuando sus datos sean válidos y su atributo secundario, si se incluye, sea compatible con el principal.
- RF-09: El sistema debe permitir reutilizar las actividades personalizadas guardadas.
- RF-10: El sistema debe permitir registrar actividades mediante la modalidad por duración, incluido el sueño mediante la cantidad de horas y minutos dormidos, sin limitar la cantidad de registros de sueño.
- RF-11: El sistema debe permitir registrar actividades mediante la modalidad por realización.
- RF-12: El sistema debe interpretar cada registro de la actividad «comida» como una comida saludable, sin exigir una confirmación adicional de esa condición.
- RF-13: El sistema debe permitir registrar actividades únicamente para hoy, ayer o anteayer.
- RF-14: El sistema debe permitir editar un registro únicamente durante los cinco minutos posteriores a su creación.
- RF-15: El sistema debe permitir eliminar un registro únicamente durante los cinco minutos posteriores a su creación.
- RF-16: El sistema debe permitir consultar las actividades registradas mediante vistas por día, semana y mes.
- RF-17: El sistema debe calcular automáticamente la XP correspondiente a cada registro según las reglas de XP definidas en este PRD.
- RF-18: El sistema debe distribuir la XP entre uno o dos atributos según los atributos asociados a la actividad.
- RF-19: El sistema debe impedir que el usuario asigne puntos de XP manualmente.
- RF-20: El sistema debe limitar la XP diaria de cada atributo a 48 XP para STR, DEX, INT y WIT; 30 XP para MEN; y 28 XP para CON, otorgando como máximo la XP restante hasta el límite de cada atributo para la fecha de la actividad.
- RF-21: El sistema debe permitir guardar actividades realizadas después de alcanzar el máximo diario de un atributo sin otorgar experiencia adicional a ese atributo durante ese día.
- RF-22: El sistema debe mantener una cantidad de XP individual para cada atributo: STR, DEX, CON, INT, WIT y MEN.
- RF-23: El sistema debe calcular de forma independiente el nivel de cada atributo entre 1 y 50: un atributo en nivel N, donde 1 ≤ N < 50, sube a N + 1 al acumular 30 × N XP dentro de ese nivel.
- RF-24: El sistema debe impedir que un atributo en nivel 50 aumente a un nivel superior.
- RF-25: El sistema debe impedir que aumente el progreso de XP de un atributo que ya se encuentra en nivel 50.
- RF-26: El sistema debe conservar la experiencia sobrante después de cada subida de nivel de un atributo.
- RF-27: El sistema debe calcular el nivel general del personaje a partir de las subidas de sus atributos, requiriendo 1 subida de atributo en nivel general 1; 2 subidas por nivel entre los niveles 2 y 4; 3 entre los niveles 5 y 8; 4 entre los niveles 9 y 14; y 5 entre los niveles 15 y 49.
- RF-28: El sistema debe representar el progreso general del personaje sin una barra de XP general independiente.
- RF-29: El sistema debe reiniciar en 0 el contador de subidas de atributos necesarias para la siguiente transición cada vez que aumenta el nivel general.
- RF-30: El sistema debe limitar el nivel general máximo del personaje a 50.
- RF-31: El sistema debe evaluar de forma independiente los días completos de inactividad de cada atributo, considerando cumplido el período para aplicar la penalización al finalizar el sexto día completo sin actividad válida asociada.
- RF-32: El sistema debe descontar 5 XP del progreso dentro del nivel actual de un atributo cuando se cumple el período de inactividad definido, sujeto al piso de 0 XP y a la conservación del nivel.
- RF-33: El sistema debe impedir que una penalización por inactividad reduzca el nivel de un atributo.
- RF-34: El sistema debe impedir que una penalización por inactividad genere XP negativa, limitando el saldo mínimo a 0 XP.
- RF-35: El sistema debe reiniciar el período de inactividad de los atributos asociados cuando se registra una actividad válida, incluso si alguno de ellos no recibe XP por haber alcanzado su máximo diario.
- RF-36: El sistema debe asignar automáticamente la clase del personaje según la cantidad y combinación de atributos equilibrados, considerando equilibrado todo atributo cuyo nivel sea al menos el 80 % del atributo de mayor nivel, y utilizando la Tabla de Clases definida en este PRD. La condición de Novato tiene prioridad: si los seis atributos permanecen en nivel 1, se asigna Novato.
- RF-37: El sistema debe mostrar la skin correspondiente a la clase actual y a la etapa visual determinada por el nivel general del personaje: Etapa I entre los niveles 1 y 14, Etapa II entre los niveles 15 y 34 y Etapa III entre los niveles 35 y 50.
- RF-38: El sistema debe actualizar la skin cuando cambie la clase o la etapa visual correspondiente al nivel general del personaje, utilizando la combinación de clase actual y etapa vigente.
- RF-39: El sistema debe ofrecer desde el comienzo un apartado que muestre el catálogo completo de clases definido en este PRD y las tres etapas visuales de cada clase.
- RF-40: El sistema debe indicar como desbloqueadas las etapas visuales alcanzadas según el nivel general del personaje y como bloqueadas las etapas superiores.
- RF-41: El sistema debe exigir una confirmación explícita antes de reiniciar la progresión del personaje.
- RF-42: El sistema debe reiniciar el nivel general, los niveles de los seis atributos, la XP, la clase y la apariencia del personaje cuando el reinicio sea confirmado.
- RF-43: El sistema debe conservar la cuenta, el nombre de usuario, las actividades personalizadas y el historial después de reiniciar la progresión del personaje.

### Catálogo inicial de actividades básicas

| Actividad | Categoría | Modalidad | Principal | Secundario |
|---|---|---|---|---|
| Entrenamiento de fuerza | Actividad física | Duración | STR | — |
| Actividad aeróbica | Actividad física | Duración | DEX | — |
| Comida saludable | Nutrición | Realización | CON | — |
| Leer / estudiar | Aprendizaje | Duración | INT | — |
| Ocio / hobby | Bienestar | Duración | WIT | — |
| Dormir | Descanso | Duración | MEN | — |
| CrossFit | Actividad física | Duración | STR | DEX |
| Artes marciales | Actividad física | Duración | DEX | STR |
| Yoga | Bienestar físico | Duración | WIT | DEX |
| Aprender una habilidad práctica | Aprendizaje | Duración | INT | DEX |
| Juegos de estrategia | Ocio / aprendizaje | Duración | WIT | INT |

Las actividades por duración usan los bloques de cinco minutos y el reparto
entre atributos de las Reglas de XP, excepto Dormir, que conserva su regla de
horas acumuladas. Comida saludable conserva su regla de 7 XP por registro con
un máximo de cuatro comidas puntuables por fecha. El usuario no modifica los
atributos preasignados de este catálogo.

### Reglas de actividades personalizadas

Una actividad personalizada es válida cuando:

- Tiene un nombre no vacío de entre 1 y 50 caracteres después de eliminar espacios iniciales y finales.
- Tiene una categoría no vacía.
- Tiene exactamente un atributo principal perteneciente a STR, DEX, INT, WIT o MEN.
- Puede no tener atributo secundario.
- Si tiene atributo secundario, debe pertenecer a STR, DEX, INT, WIT o MEN y ser diferente del principal.
- CON está reservado exclusivamente para la actividad básica «comida» y no puede asignarse a actividades personalizadas.

Para el MVP, cualquier par de atributos diferentes dentro de STR, DEX, INT, WIT y MEN se considera compatible. Por ejemplo, STR + DEX, STR + MEN e INT + WIT son válidos; STR + STR es inválido.

### Reglas de XP

Las cantidades de XP indicadas se aplican a atributos por debajo del nivel 50. Al llegar a nivel 50, el atributo no aumenta su nivel ni su progreso, aunque las actividades pueden seguir registrándose.

Los topes y el conteo de comidas se calculan por la fecha de la actividad, en días calendario UTC−3, también cuando se registra una actividad de ayer o anteayer. El margen diario disponible es la diferencia entre el tope del atributo y la XP ya otorgada a ese atributo para esa fecha.

Las actividades por duración, excepto el sueño, otorgan 1 XP por cada 5 minutos completos del registro. En actividades con dos atributos, 2/3 de la XP corresponden al atributo principal y 1/3 al secundario, redondeando el aporte del principal hacia arriba y asignando el resto al secundario. Este cálculo se realiza antes de aplicar los topes diarios y de nivel.

Ejemplos: 15 minutos otorgan 3 XP; 30 minutos otorgan 6 XP; 30 minutos con dos atributos otorgan 4 XP al principal y 2 XP al secundario.

El sueño conserva su regla específica: 3 XP a MEN por cada 60 minutos acumulados pendientes de contabilizar.

Los minutos pendientes de sueño se acumulan únicamente entre registros de la misma fecha de actividad en UTC−3. Los sobrantes no pasan a otra fecha: 30 minutos de ayer y 30 de hoy no otorgan XP por sueño.

| Registro | Aporte antes de aplicar el tope diario |
|---|---|
| 30 minutos de una actividad simple de STR, DEX, INT o WIT | 6 XP al atributo asociado |
| 30 minutos de una actividad con STR principal y DEX secundario | 20 minutos y 4 XP a STR; 10 minutos y 2 XP a DEX |
| Cada una de las primeras cuatro comidas saludables de una fecha | 7 XP a CON |
| Quinta comida saludable y posteriores de la misma fecha | 0 XP adicional a CON; el registro se conserva |
| 60 minutos acumulados de sueño pendientes de contabilizar | 3 XP a MEN |

CON solo obtiene XP mediante la actividad básica de comida saludable, denominada «comida» en este PRD. Cada registro de «comida» representa una comida declarada saludable. Sin registros de comidas en una fecha, se contabilizan 0 comidas saludables y 0 XP por comidas; con dos registros, se contabilizan dos comidas saludables. No se rechazan registros por superar cuatro comidas en el día: solo se limita su aporte de XP.

El aporte efectivo a cada atributo es el menor entre la XP calculada y el margen diario disponible. En actividades con dos atributos, cada uno aplica su propio límite. El nivel y el progreso de XP de un atributo en nivel 50 se rigen por RF-24 y RF-25.

### Regla de recálculo de progresión

Cuando un registro es editado o eliminado, el sistema debe reconstruir la progresión derivada del ciclo actual del personaje utilizando los registros vigentes.

Los registros se procesan primero por fecha de actividad en UTC−3 y, dentro de una misma fecha, por fecha y hora de creación.

Un registro editado se procesa utilizando sus valores actuales. Un registro eliminado se excluye del cálculo.

Durante el recálculo se vuelven a aplicar las reglas de XP, topes diarios, niveles de atributos, nivel general, inactividad, clase y etapa visual definidas en este PRD.

Los registros pertenecientes a ciclos anteriores a un reinicio del personaje permanecen visibles en el historial, pero no participan del recálculo de la progresión actual.

### Tabla de Clases

Primero se verifica la excepción inicial: si los seis atributos están en nivel 1, la clase es **Novato**. Esta condición tiene prioridad sobre el conteo de atributos equilibrados.

En cualquier otro caso, un atributo está equilibrado si su nivel es mayor o igual al 80 % del mayor nivel entre los seis atributos. La comparación se realiza contra ese valor, sin redondearlo. Se cuenta cuántos atributos cumplen la condición; el atributo de mayor nivel siempre está incluido, por lo que el conteo va de 1 a 6.

#### Clases puras: exactamente un atributo equilibrado

| Atributo | Clase |
|---|---|
| STR | Guerrero |
| DEX | Explorador |
| CON | Guardián |
| INT | Sabio |
| WIT | Monje |
| MEN | Místico |

#### Clases combinadas: exactamente dos atributos equilibrados

El orden de los dos atributos no cambia la clase asignada.

| Atributos | Clase |
|---|---|
| STR + DEX | Maestro de Armas |
| STR + CON | Paladín |
| STR + INT | Guerrero Arcano |
| STR + WIT | Luchador Monje |
| STR + MEN | Guerrero Místico |
| DEX + CON | Ágil Guardián |
| DEX + INT | Artífice |
| DEX + WIT | Ninja |
| DEX + MEN | Explorador de Sueños |
| CON + INT | Guardián de la Sabiduría |
| CON + WIT | Monje Guardián |
| CON + MEN | Druida |
| INT + WIT | Ilusionista |
| INT + MEN | Oráculo |
| WIT + MEN | Monje Espiritual |

#### Clases por equilibrio múltiple

| Cantidad de atributos equilibrados | Clase |
|---|---|
| 3 | Aventurero Versátil |
| 4 | Maestro del Equilibrio |
| 5 | Héroe Ascendente |
| 6 | Héroe Integral |

### Etapas visuales y catálogo

Cada clase, incluido Novato, tiene tres skins identificadas por su clase y etapa. La skin activa depende de la clase actual y del nivel general actual.

| Etapa | Nivel general para usar la skin | Desbloqueo en el catálogo |
|---|---|---|
| Etapa I | 1–14 | Desde nivel 1 |
| Etapa II | 15–34 | Desde nivel 15 |
| Etapa III | 35–50 | Desde nivel 35 |

Todas las clases pueden consultarse desde el comienzo. Lo que se desbloquea según el nivel general son las etapas visuales: en nivel 8 solo Etapa I; en nivel 20, Etapas I y II; y en nivel 40, las tres etapas.

## Requerimientos No Funcionales

- RNF-01: En el 95 % de las aperturas de la aplicación, la pantalla principal debe quedar disponible para interacción en un máximo de 3 segundos.
- RNF-02: El 95 % de los registros, ediciones y eliminaciones de actividades debe guardarse y reflejar el nuevo progreso en un máximo de 2 segundos.
- RNF-03: El 95 % de las consultas de historial debe responder en un máximo de 3 segundos.
- RNF-04: Una actividad guardada previamente debe poder registrarse con un máximo de tres interacciones desde la pantalla principal y en una mediana de 15 segundos o menos durante una prueba con al menos diez usuarios.
- RNF-05: El servicio debe alcanzar una disponibilidad mensual mínima del 99,5 %, excluyendo mantenimientos programados informados con antelación.
- RNF-06: Una prueba de 1.000 reintentos o dobles interacciones sobre la misma operación debe producir 0 registros y 0 asignaciones de XP duplicados.
- RNF-07: El 100 % de las comunicaciones debe utilizar HTTPS con TLS 1.2 o superior; las credenciales de Google no deben almacenarse y los tokens no deben aparecer en código, respuestas ni registros de aplicación.
- RNF-08: El 100 % de los casos de la suite de autorización debe impedir que una cuenta consulte, cree, modifique o elimine atributos, actividades o historial pertenecientes a otra cuenta.
- RNF-09: El 100 % de los casos de prueba relacionados con días, edición e inactividad debe utilizar UTC−3 como zona horaria de negocio.
- RNF-10: El 100 % de los flujos críticos debe completarse sin errores funcionales en dispositivos o emuladores con Android 10 (API 29) o superior. Se consideran flujos críticos: iniciar sesión, registrar una actividad, editarla o eliminarla dentro del período permitido, consultar el historial, actualizar XP/niveles/clase y reiniciar el personaje.
- RNF-11: La aplicación debe mantener al menos un 99,5 % de sesiones sin cierres inesperados, medido sobre un mínimo de 1.000 sesiones de prueba ejecutadas en dispositivos o emuladores con Android 10 (API 29) o superior.
- RNF-12: Los textos y controles deben alcanzar una relación de contraste mínima de 4,5:1 y ninguna información esencial debe comunicarse únicamente mediante color.
- RNF-13: Las pruebas de reinicio de aplicación y servicio deben producir 0 pérdidas de registros confirmados.

## Criterios de Aceptación

- AC-01 (RF-01): Dada una identidad de Google válida no registrada, cuando el usuario completa el registro, entonces se crea una cuenta de LUL sin solicitar una contraseña propia.
- AC-02 (RF-02): Dada una cuenta de LUL existente vinculada a Google, cuando su titular completa la autenticación con esa identidad de Google, entonces accede a su cuenta sin introducir una contraseña propia de LUL.
- AC-03 (RF-03): Dada una identidad de Google ya registrada, cuando el usuario vuelve a autenticarse, entonces el sistema recupera la misma cuenta y no crea otra.
- AC-04 (RF-04): Dado un nombre de usuario ya utilizado, cuando otra persona intenta confirmarlo, entonces el sistema lo rechaza y no crea el perfil.
- AC-05 (RF-05): Dado un nombre de usuario disponible, cuando el usuario lo confirma y se crea el personaje, entonces existe exactamente un personaje con nivel general 1, los seis atributos en nivel 1 con 0 XP y clase Novato.
- AC-06 (RF-06): Dado el catálogo inicial, cuando el usuario selecciona una actividad básica, entonces se muestran su modalidad, categoría y atributos preasignados sin permitirle modificar la puntuación.
- AC-07 (RF-07): Dado el flujo de creación de una actividad personalizada, cuando el usuario define su nombre, categoría y atributo principal, entonces puede completar la definición sin incluir un atributo secundario.
- AC-08 (RF-07, RF-08): Dada una actividad personalizada que cumple las Reglas de actividades personalizadas definidas en este PRD y no incluye atributo secundario, cuando el usuario la guarda, entonces queda almacenada y disponible para registros posteriores.
- AC-09 (RF-07, RF-08): Dada una actividad personalizada que cumple las Reglas de actividades personalizadas definidas en este PRD e incluye un atributo secundario del conjunto permitido diferente del principal, cuando el usuario la guarda, entonces queda almacenada y disponible para registros posteriores.
- AC-10 (RF-08): Dada una actividad personalizada cuyo atributo secundario es igual al principal, cuando el usuario intenta guardarla, entonces el sistema rechaza la operación y la actividad no queda almacenada.
- AC-11 (RF-09): Dada una actividad personalizada guardada previamente, cuando el usuario vuelve a seleccionarla, entonces puede utilizarla para realizar un nuevo registro sin crear nuevamente la actividad.
- AC-12 (RF-10): Dada una actividad por duración, cuando el usuario informa una duración mayor que 0, entonces el registro se guarda con los minutos indicados.
- AC-13 (RF-10): Dado un día con registros de sueño previos, cuando el usuario registra otro período de sueño con una duración mayor que 0, entonces el registro queda guardado con la duración indicada, aunque MEN ya haya alcanzado su tope diario de XP.
- AC-14 (RF-11): Dada una actividad por realización, cuando el usuario confirma que la realizó, entonces se guarda una sola ocurrencia sin solicitar duración.
- AC-15 (RF-12): Dada la actividad «comida», cuando el usuario registra una ocurrencia, entonces el sistema la contabiliza como una comida saludable sin solicitar una confirmación adicional.
- AC-16 (RF-12, RF-17, RF-20, RF-21): Dado que el usuario ya registró al menos cuatro comidas saludables en un mismo día calendario, cuando registra otra comida para esa fecha, entonces la actividad queda registrada pero CON recibe 0 XP adicional.
- AC-17 (RF-13): Dada una actividad válida, cuando el usuario elige hoy, ayer o anteayer, entonces el sistema la registra en esa fecha.
- AC-18 (RF-13): Dada una actividad válida, cuando el usuario intenta registrarla en una fecha distinta de hoy, ayer o anteayer, entonces el sistema rechaza el registro.
- AC-19 (RF-14): Dado un registro creado hace menos de cinco minutos, cuando el usuario lo edita, entonces el registro conserva los nuevos valores y la progresión resultante coincide con la obtenida al reprocesar los registros vigentes del ciclo actual según la Regla de recálculo de progresión definida en este PRD.
- AC-20 (RF-14): Dado un registro creado hace cinco minutos o más, cuando el usuario intenta editarlo, entonces el sistema impide la operación y conserva el registro.
- AC-21 (RF-15): Dado un registro creado hace menos de cinco minutos, cuando el usuario lo elimina, entonces el sistema elimina el registro, lo excluye del cálculo y la progresión resultante coincide con la obtenida al reprocesar los registros vigentes del ciclo actual según la Regla de recálculo de progresión definida en este PRD.
- AC-22 (RF-15): Dado un registro creado hace cinco minutos o más, cuando el usuario intenta eliminarlo, entonces el sistema impide la operación y conserva el registro.
- AC-23 (RF-16): Dadas actividades pertenecientes a distintas fechas, cuando el usuario abre una vista diaria, semanal o mensual, entonces se muestran únicamente las actividades comprendidas en el período seleccionado.
- AC-24 (RF-16, RF-43): Dado un reinicio del personaje, cuando el usuario consulta un período anterior, entonces el historial continúa visible y sus actividades no vuelven a otorgar XP.
- AC-25 (RF-17): Dado un atributo STR, DEX, INT o WIT por debajo del nivel 50 y con al menos 6 XP disponibles antes de alcanzar su máximo diario en la fecha de la actividad, cuando el usuario registra 30 minutos de una actividad simple asociada a ese atributo, entonces el sistema otorga 6 XP.
- AC-26 (RF-17, RF-20): Dado un atributo STR, DEX, INT o WIT por debajo del nivel 50 y con un margen diario disponible mayor o igual a 0 y menor que 6 XP para la fecha de la actividad, cuando una actividad válida genera 6 XP, entonces el sistema otorga únicamente la XP restante hasta alcanzar 48 XP diarias.
- AC-27 (RF-18): Dados 30 minutos de una actividad con STR como atributo principal y DEX como secundario, ambos por debajo del nivel 50 y con margen diario suficiente en la fecha de la actividad, cuando el sistema calcula el aporte, entonces asigna 20 minutos y 4 XP a STR y 10 minutos y 2 XP a DEX.
- AC-28 (RF-18, RF-20): Dada una actividad con dos atributos asociados por debajo del nivel 50, al menos uno de los cuales tiene menos margen diario disponible que la XP que le corresponde, cuando el sistema calcula el aporte para la fecha de la actividad, entonces cada atributo recibe el menor valor entre su aporte calculado y su propio margen diario disponible.
- AC-29 (RF-12, RF-17): Dado que CON está por debajo del nivel 50 y se registraron menos de cuatro comidas saludables en la fecha de la actividad, cuando se registra una nueva comida para esa fecha, entonces CON recibe exactamente 7 XP.
- AC-30 (RF-12, RF-17): Dado un día sin registros de comidas saludables, cuando el sistema calcula la XP de CON por comidas de esa fecha, entonces el aporte es 0 XP.
- AC-31 (RF-17): Dado que MEN está por debajo del nivel 50 y tiene al menos 3 XP disponibles antes de alcanzar su máximo diario en la fecha del sueño, cuando se acumulan 60 minutos válidos de sueño pendientes de contabilizar, entonces MEN recibe 3 XP.
- AC-32 (RF-17, RF-20): Dado que MEN está por debajo del nivel 50 y tiene un margen diario disponible mayor o igual a 0 y menor que 3 XP para la fecha del sueño, cuando se acumulan 60 minutos válidos de sueño pendientes de contabilizar, entonces el sistema otorga únicamente la XP restante hasta alcanzar 30 XP diarias.
- AC-33 (RF-19): Dado un usuario autenticado que intenta crear o modificar un registro incluyendo un valor de XP proporcionado manualmente, cuando el sistema procesa la operación, entonces ignora o rechaza ese valor y calcula la XP únicamente según las reglas definidas en el PRD.
- AC-34 (RF-20): Dado un día calendario en UTC−3, cuando STR, DEX, INT o WIT alcanza 48 XP, MEN alcanza 30 XP o CON alcanza 28 XP para esa fecha, entonces el sistema no otorga más XP a ese atributo por actividades de esa misma fecha, aunque se registren posteriormente.
- AC-35 (RF-21): Dado un atributo que alcanzó su límite diario para una fecha, cuando el usuario registra otra actividad válida de esa misma fecha asociada a ese atributo, entonces la actividad queda en el historial, reinicia su inactividad y aporta 0 XP adicional al atributo.
- AC-36 (RF-22): Dados los valores de XP de los seis atributos y una actividad simple válida que aporta XP a uno solo, cuando se registra esa actividad sin que corresponda una penalización por inactividad, entonces solo cambia la XP del atributo asociado y los otros cinco conservan su XP.
- AC-37 (RF-23): Dado un atributo en nivel N, donde 1 ≤ N < 50, cuando únicamente ese atributo acumula 30 × N XP dentro de ese nivel, entonces sube a N + 1 sin aumentar los niveles de los demás atributos.
- AC-38 (RF-24, RF-25): Dado un atributo en nivel 50, cuando una actividad válida generaría XP para ese atributo, entonces conserva el nivel 50 y su progreso de XP no aumenta.
- AC-39 (RF-26): Dado un atributo en nivel 1 con 28 XP, cuando recibe 7 XP, entonces sube a nivel 2 y conserva 5 XP para la siguiente subida.
- AC-40 (RF-27): Dado un personaje en nivel general 1, cuando cualquiera de sus atributos sube por primera vez, entonces el personaje alcanza el nivel general 2.
- AC-41 (RF-27): Dado un personaje en nivel general G, donde 1 ≤ G < 50, cuyo contador está a una subida del requisito definido para ese nivel, cuando se registra la última subida necesaria, entonces el nivel general aumenta a G + 1.
- AC-42 (RF-29): Dado un personaje cuyo nivel general acaba de aumentar, cuando finaliza la transición de nivel, entonces el contador de subidas para el nuevo nivel queda en 0.
- AC-43 (RF-30): Dado un personaje en nivel general 49 cuyo contador está a una subida de atributo de completar el requisito de ese nivel, cuando se registra la última subida necesaria, entonces el personaje alcanza el nivel general 50.
- AC-44 (RF-30): Dado un personaje en nivel general 50, cuando posteriormente se produce una nueva subida válida de cualquiera de sus atributos, entonces el nivel general permanece en 50 y no se inicia una nueva transición de nivel general.
- AC-45 (RF-31): Dado un atributo con cuatro días completos sin actividad válida asociada, cuando finaliza el quinto día completo sin actividad, entonces el sistema determina que todavía no se cumplió el período de seis días requerido para penalizarlo.
- AC-46 (RF-31): Dado un atributo con cinco días completos sin actividad válida asociada, cuando finaliza el sexto día completo sin actividad, entonces el sistema determina que se cumplió el período de inactividad para aplicar la penalización.
- AC-47 (RF-32): Dado un atributo con al menos 5 XP dentro de su nivel actual y cinco días completos sin actividad válida asociada, cuando finaliza el sexto día completo sin actividad, entonces pierde 5 XP de su progreso dentro del nivel actual.
- AC-48 (RF-33): Dado un atributo en nivel N, cuando se aplica una penalización por inactividad, entonces conserva el nivel N.
- AC-49 (RF-34): Dado un atributo con menos de 5 XP dentro de su nivel actual, cuando corresponde aplicar la penalización por inactividad, entonces su XP queda en 0.
- AC-50 (RF-35): Dada una actividad compuesta válida, cuando el usuario la registra, entonces se reinicia el período de inactividad de sus dos atributos aunque alguno no reciba XP por haber alcanzado el máximo diario.
- AC-51 (RF-36): Dado un personaje cuyos seis atributos permanecen en nivel 1, cuando el sistema calcula su clase, entonces asigna la clase Novato.
- AC-52 (RF-36): Dado un personaje que ya no es Novato y en el que exactamente un atributo tiene un nivel igual o superior al 80 % del mayor nivel actual entre los seis atributos, cuando el sistema calcula la clase, entonces asigna la clase pura de ese atributo según la Tabla de Clases.
- AC-53 (RF-36): Dado un personaje en el que exactamente dos atributos tienen un nivel igual o superior al 80 % del atributo de mayor nivel, cuando el sistema calcula la clase, entonces asigna la clase combinada correspondiente a ese par según la Tabla de Clases.
- AC-54 (RF-36): Dado un personaje en el que exactamente tres atributos tienen un nivel igual o superior al 80 % del atributo de mayor nivel, cuando el sistema calcula la clase, entonces asigna Aventurero Versátil.
- AC-55 (RF-36): Dado un personaje en el que exactamente cuatro atributos tienen un nivel igual o superior al 80 % del atributo de mayor nivel, cuando el sistema calcula la clase, entonces asigna Maestro del Equilibrio.
- AC-56 (RF-36): Dado un personaje en el que exactamente cinco atributos tienen un nivel igual o superior al 80 % del atributo de mayor nivel, cuando el sistema calcula la clase, entonces asigna Héroe Ascendente.
- AC-57 (RF-36): Dado un personaje en el que los seis atributos tienen un nivel igual o superior al 80 % del atributo de mayor nivel y el personaje ya no es Novato, cuando el sistema calcula la clase, entonces asigna Héroe Integral.
- AC-58 (RF-37): Dado un personaje con una clase asignada y un nivel general entre 1 y 50, cuando se muestra su apariencia, entonces la skin pertenece a su clase actual y corresponde a Etapa I para niveles 1–14, Etapa II para niveles 15–34 o Etapa III para niveles 35–50.
- AC-59 (RF-38): Dado un personaje cuya combinación de atributos provoca un cambio de clase sin cambio de nivel general, cuando el sistema recalcula su clase, entonces muestra la skin de la nueva clase y conserva la etapa visual correspondiente a su nivel general.
- AC-60 (RF-38): Dado un personaje cuyo nivel general cambia por una subida o corrección, cuando el sistema actualiza su apariencia, entonces la skin de su clase actual corresponde a la etapa definida para el nivel general resultante.
- AC-61 (RF-39): Dado el catálogo de clases definido en este PRD, cuando el usuario abre el apartado de clases, entonces visualiza todas las clases, sus atributos o condiciones de equilibrio y las tres etapas visuales de cada una.
- AC-62 (RF-40): Dado un personaje con nivel general entre 1 y 50, cuando consulta el catálogo, entonces Etapa I aparece desbloqueada desde nivel 1, Etapa II desde nivel 15 y Etapa III desde nivel 35, y las etapas cuyo umbral aún no alcanzó aparecen bloqueadas.
- AC-63 (RF-41): Dado que el usuario solicita reiniciar el personaje, cuando todavía no confirmó explícitamente la operación, entonces el sistema no modifica la progresión.
- AC-64 (RF-41): Dado que el usuario solicita reiniciar el personaje, cuando cancela la confirmación, entonces el sistema no realiza cambios.
- AC-65 (RF-42): Dado que el usuario confirma el reinicio, cuando finaliza el proceso, entonces el nivel general y los seis atributos vuelven a 1, la XP vuelve a 0 y la clase y apariencia cambian a Novato.
- AC-66 (RF-43): Dado que el reinicio del personaje fue completado, cuando el usuario vuelve a acceder a su cuenta, entonces la cuenta, el nombre de usuario, las actividades personalizadas y el historial se conservan.
- AC-67 (RNF-08): Dadas dos cuentas distintas A y B autenticadas, cuando el usuario A intenta consultar atributos, actividades o historial pertenecientes al usuario B, entonces el sistema rechaza el acceso y no devuelve ningún dato de B.
- AC-68 (RNF-08): Dadas dos cuentas distintas A y B autenticadas, cuando el usuario A intenta crear, modificar o eliminar atributos, actividades o historial pertenecientes al usuario B, entonces el sistema rechaza la operación y los datos de B permanecen sin cambios.
- AC-69 (RF-07, RF-08): Dada una actividad personalizada en la que el usuario intenta seleccionar CON como atributo principal o secundario, cuando intenta guardar la actividad, entonces el sistema rechaza la operación y la actividad no queda almacenada.

- AC-70 (RF-04): Dado un nombre de usuario disponible, cuando el usuario lo confirma, entonces ese nombre queda asignado a su cuenta.
- AC-71 (RF-28): Dado un personaje creado, cuando el usuario consulta su progreso general, entonces no se muestra una barra de XP general independiente.

## Fuera de Alcance

- Selección de una clase objetivo.
- Recomendaciones de actividades.
- Notificaciones asociadas a recomendaciones de actividades.
- Registro de actividades laborales.
- Planificación de tareas futuras, agenda y calendario.
- Aplicación para iOS o versión web.
- Funcionamiento completo sin conexión a Internet.
- Más de un personaje por cuenta.
- Visualización pública de los atributos, las actividades o el historial de otros usuarios.
- Verificación de actividades mediante fotografías, GPS u otras pruebas.
- Integración con dispositivos wearables o aplicaciones de salud.
- Ligas privadas creadas mediante invitación.
- Torneos globales mensuales, ascensos y descensos de liga.
- Monedas, compras con dinero real y personalización cosmética independiente de la clase.
- Ligas globales.
- Ligas de amigos.
- Sistema de Amigos.

Estas funcionalidades podrán evaluarse para versiones posteriores.

## Riesgos y Dependencias

### Riesgos

- **Riesgo:** El registro manual puede resultar tedioso y provocar abandono.
  **Mitigación:** reutilizar actividades guardadas, limitar el flujo frecuente a tres interacciones y medir el tiempo real de registro (RNF-04).

- **Riesgo:** El usuario puede registrar actividades inexistentes o explotar el sistema únicamente para obtener puntos.
  **Mitigación:** utilizar confianza declarada, puntuación calculada por el sistema, límites diarios y análisis de patrones anómalos, sin afirmar que la actividad fue verificada (RF-17, RF-19 y RF-20).

- **Riesgo:** La velocidad de progreso puede ser demasiado lenta, rápida o favorecer ciertos atributos.
  **Mitigación:** simular distintas rutinas antes del desarrollo final y ajustar tasas, límites y costos de nivel con datos de pruebas.

- **Riesgo:** La penalización por inactividad puede desalentar el regreso.
  **Mitigación:** impedir la pérdida de niveles, limitar el descuento a la XP actual y validar la regla con usuarios (RF-32, RF-33 y RF-34).

- **Riesgo:** La gamificación puede volverse más importante que el bienestar real del usuario.
  **Mitigación:** evitar penalizaciones severas, no exigir pruebas invasivas y evaluar percepción de ayuda, no solo cantidad de registros.

- **Riesgo:** Una falla de autorización puede exponer información privada.
  **Mitigación:** aplicar controles de acceso en el servidor y mantener una suite específica con 0 accesos indebidos (RNF-08).

- **Riesgo:** La cantidad de clases, evoluciones y skins puede aumentar el costo del MVP.
  **Mitigación:** utilizar la Tabla de Clases y las tres etapas visuales definidas en este PRD, y reutilizar una estructura visual común antes de producir todos los recursos.

### Dependencias

- Servicio de autenticación de Google.
- Backend y almacenamiento persistente para cuentas, actividades, progreso.
- Conexión a Internet para registrar, sincronizar y consultar información.
- Catálogo aprobado de categorías y actividades.
- Recursos visuales para las tres etapas de Novato y de las demás clases.
- Simulaciones de rutinas para validar la curva de progresión y las penalizaciones.
- Dataset de pruebas temporales para validar UTC−3, cargas atrasadas.
