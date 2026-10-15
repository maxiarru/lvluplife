package com.lvluplife.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun ActivityHub(account: Account, padding: PaddingValues, onSignOut: () -> Unit) {
    val repository = remember { ActivityRepository() }
    val progressRepository = remember { ProgressRepository() }
    var today by remember { mutableStateOf<LocalDate?>(null) }
    var progress by remember { mutableStateOf<ProgressResult?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    var viewingClass by remember { mutableStateOf("Novato") }
    var options by remember { mutableStateOf(BasicActivities.all) }
    var tab by remember { mutableStateOf("Registrar") }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ActivityRecord?>(null) }
    var selected by remember { mutableStateOf(BasicActivities.all.first()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("0") }
    var activityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(tab) {
        repository.today { result ->
            result.onSuccess { today = it; if (editing == null) selectedDate = it }
                .onFailure { error = it.message }
        }
    }
    DisposableEffect(Unit) {
        val listener = repository.watchCustom { result ->
            result.onSuccess { options = BasicActivities.all + it }.onFailure { error = it.message }
        }
        onDispose { listener.remove() }
    }
    DisposableEffect(today) {
        val listener = today?.let { date -> progressRepository.watch(date) { result ->
            result.onSuccess { progress = it }.onFailure { error = it.message }
        } }
        onDispose { listener?.remove() }
    }
    fun resetForm() {
        editing = null
        selected = BasicActivities.all.first()
        selectedDate = today
        hours = "0"
        minutes = "0"
    }
    Column(
        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Hola, ${account.username}", style = MaterialTheme.typography.headlineMedium)
        Text("${progress?.characterClass ?: account.character.characterClass} · Nivel ${progress?.generalLevel ?: account.character.generalLevel}")
        CharacterArt.forMainScreen(progress?.characterClass ?: account.character.characterClass)?.let { art ->
            Image(painter = painterResource(art), contentDescription = "Personaje ${progress?.characterClass ?: account.character.characterClass}",
                modifier = Modifier.fillMaxWidth().height(340.dp), contentScale = ContentScale.Fit)
        }
        Text((progress?.attributes ?: account.character.attributes).entries.joinToString(" · ") { (name, attribute) -> "$name ${attribute.level}" })
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Registrar", "Historial", "Personalizar", "Progreso", "Clases").forEach { name ->
                FilterChip(selected = tab == name, onClick = { tab = name; error = null }, label = { Text(name) })
            }
        }
        if (message != null) Text(message!!, color = MaterialTheme.colorScheme.primary)
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
        when (tab) {
            "Registrar" -> {
                Text(if (editing == null) "Registrar actividad" else "Editar registro", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { activityPicker = true }, enabled = editing?.kind != "meal") {
                    Text("Actividad: ${selected.name}")
                }
                if (activityPicker) {
                    AlertDialog(onDismissRequest = { activityPicker = false }, title = { Text("Elegí una actividad") },
                        text = {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                options.filter { editing?.kind != "duration" || it.kind == "duration" }.forEach { option ->
                                    OutlinedButton(onClick = { selected = option; activityPicker = false }, modifier = Modifier.fillMaxWidth()) {
                                        Text(option.name)
                                    }
                                }
                            }
                        }, confirmButton = { OutlinedButton(onClick = { activityPicker = false }) { Text("Cerrar") } })
                }
                Text("${selected.category} · ${if (selected.kind == "meal") "Por realización" else "Por duración"} · ${selected.primary}${selected.secondary?.let { " + $it" } ?: ""}")
                Text("Fecha de la actividad")
                val serverDay = today
                if (serverDay != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Hoy", 1 to "Ayer", 2 to "Anteayer").forEach { (offset, label) ->
                            val date = serverDay.minusDays(offset.toLong())
                            FilterChip(selected = selectedDate == date, onClick = { selectedDate = date }, label = { Text(label) })
                        }
                    }
                    Text(selectedDate?.toString().orEmpty())
                }
                if (selected.kind == "duration") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Horas") },
                            modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Minutos") },
                            modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                val parsedHours = if (hours.isBlank()) 0 else hours.toIntOrNull()
                val parsedMinutes = if (minutes.isBlank()) 0 else minutes.toIntOrNull()
                val durationValid = selected.kind == "meal" ||
                    (parsedHours != null && parsedHours in 0..24 && parsedMinutes != null && parsedMinutes in 0..59 &&
                        parsedHours * 60 + parsedMinutes in 1..1440)
                Button(enabled = !busy && selectedDate != null && durationValid, onClick = {
                    busy = true; error = null; message = null
                    repository.mutate(if (editing == null) "create" else "edit", editing, selectedDate,
                        selected, parsedHours, parsedMinutes) { result ->
                        busy = false
                        result.onSuccess { message = "Registro guardado"; resetForm() }
                            .onFailure { error = it.message ?: "No se pudo guardar" }
                    }
                }) { Text(if (editing == null) "Guardar registro" else "Guardar cambios") }
                if (editing != null) OutlinedButton(onClick = { resetForm() }) { Text("Cancelar edición") }
            }
            "Historial" -> HistoryView(repository, today, onEdit = { record ->
                editing = record
                selected = options.find { if (it.custom) it.id == record.customId else it.id == record.catalogId }
                    ?: BasicActivities.all.first()
                selectedDate = LocalDate.parse(record.date)
                hours = (record.minutes / 60).toString()
                minutes = (record.minutes % 60).toString()
                tab = "Registrar"
                message = null
            }, onError = { error = it }, onMessage = { message = it })
            "Personalizar" -> CustomActivityView(repository, busy, options.filter { it.custom },
                onBusy = { busy = it }, onError = { error = it }, onMessage = { message = it })
            "Progreso" -> {
                val current = progress
                if (current == null) Text("Cargando progreso…") else {
                    Text("${current.characterClass} · Nivel general ${current.generalLevel} · Etapa ${listOf("I", "II", "III")[ProgressEngine.stage(current.generalLevel) - 1]}",
                        style = MaterialTheme.typography.titleLarge)
                    ProgressEngine.attributes.forEach { key ->
                        val value = current.attributes.getValue(key)
                        Text("$key · Nivel ${value.level} · ${value.xp}/${if (value.level == 50) 0 else value.level * 30} XP")
                    }
                    OutlinedButton(onClick = { confirmReset = true }) { Text("Reiniciar progresión") }
                }
            }
            "Clases" -> {
                Text("Clases", style = MaterialTheme.typography.titleLarge)
                Text("${viewingClass} · ${ProgressEngine.classCondition(viewingClass)}")
                CharacterArt.forMainScreen(viewingClass)?.let { art ->
                    Image(painter = painterResource(art), contentDescription = "Clase $viewingClass",
                        modifier = Modifier.fillMaxWidth().height(260.dp), contentScale = ContentScale.Fit)
                }
                ProgressEngine.allClasses.forEach { name ->
                    OutlinedButton(onClick = { viewingClass = name }) { Text(name) }
                    Text(ProgressEngine.classCondition(name) + " · " + (1..3).joinToString(" · ") { stage ->
                        val unlocked = (progress?.generalLevel ?: account.character.generalLevel) >= when (stage) { 1 -> 1; 2 -> 15; else -> 35 }
                        "${listOf("I", "II", "III")[stage - 1]} ${if (unlocked) "desbloqueada" else "bloqueada"}"
                    })
                }
            }
        }
        if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false },
            title = { Text("¿Reiniciar progresión?") },
            text = { Text("El personaje vuelve a nivel 1. Tu cuenta, nombre, actividades personalizadas e historial se conservan.") },
            confirmButton = { Button(onClick = {
                confirmReset = false; busy = true
                progressRepository.reset { result ->
                    busy = false
                    result.onSuccess { message = "Progresión reiniciada" }
                        .onFailure { error = it.message ?: "No se pudo reiniciar" }
                }
            }, enabled = !busy) { Text("Confirmar reinicio") } },
            dismissButton = { OutlinedButton(onClick = { confirmReset = false }) { Text("Cancelar") } })
        OutlinedButton(onClick = onSignOut) { Text("Cerrar sesión") }
    }
}

@Composable
private fun CustomActivityView(
    repository: ActivityRepository, busy: Boolean, saved: List<ActivityOption>,
    onBusy: (Boolean) -> Unit, onError: (String?) -> Unit, onMessage: (String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var primary by remember { mutableStateOf("STR") }
    var secondary by remember { mutableStateOf<String?>(null) }
    Text("Nueva actividad personalizada", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(name, { name = it }, label = { Text("Nombre (1 a 50 caracteres)") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(category, { category = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
    Text("Atributo principal")
    listOf("STR", "DEX", "INT", "WIT", "MEN").forEach { attribute ->
        FilterChip(primary == attribute, onClick = { primary = attribute; if (secondary == attribute) secondary = null }, label = { Text(attribute) })
    }
    Text("Atributo secundario (opcional)")
    listOf<String?>(null, "STR", "DEX", "INT", "WIT", "MEN").filter { it != primary }.forEach { attribute ->
        FilterChip(secondary == attribute, onClick = { secondary = attribute }, label = { Text(attribute ?: "Ninguno") })
    }
    Button(enabled = !busy && name.trim().length in 1..50 && category.isNotBlank(), onClick = {
        onBusy(true); onError(null); onMessage(null)
        repository.createCustom(name, category, primary, secondary) { result ->
            onBusy(false)
            result.onSuccess { onMessage("Actividad guardada"); name = ""; category = ""; secondary = null }
                .onFailure { onError(it.message ?: "No se pudo guardar") }
        }
    }) { Text("Guardar actividad") }
    Text("Mis actividades", style = MaterialTheme.typography.titleMedium)
    saved.forEach { Text("${it.name} · ${it.category} · ${it.primary}${it.secondary?.let { second -> " + $second" } ?: ""}") }
}

@Composable
private fun HistoryView(
    repository: ActivityRepository, today: LocalDate?,
    onEdit: (ActivityRecord) -> Unit, onError: (String?) -> Unit, onMessage: (String?) -> Unit,
) {
    var period by remember { mutableStateOf("Día") }
    var anchor by remember(today) { mutableStateOf(today ?: LocalDate.now(java.time.ZoneOffset.ofHours(-3))) }
    var records by remember { mutableStateOf(emptyList<ActivityRecord>()) }
    var deleting by remember { mutableStateOf<String?>(null) }
    val start = when (period) {
        "Semana" -> anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        "Mes" -> anchor.withDayOfMonth(1)
        else -> anchor
    }
    val end = when (period) { "Semana" -> start.plusWeeks(1); "Mes" -> start.plusMonths(1); else -> start.plusDays(1) }
    DisposableEffect(start, end) {
        val listener = repository.watchHistory(start, end) { result ->
            result.onSuccess { records = it }.onFailure { onError(it.message ?: "No se pudo cargar el historial") }
        }
        onDispose { listener.remove() }
    }
    Text("Historial", style = MaterialTheme.typography.titleLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Día", "Semana", "Mes").forEach { value ->
            FilterChip(period == value, onClick = { period = value }, label = { Text(value) })
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { anchor = when (period) { "Semana" -> anchor.minusWeeks(1); "Mes" -> anchor.minusMonths(1); else -> anchor.minusDays(1) } }) { Text("Anterior") }
        OutlinedButton(onClick = { anchor = when (period) { "Semana" -> anchor.plusWeeks(1); "Mes" -> anchor.plusMonths(1); else -> anchor.plusDays(1) } }) { Text("Siguiente") }
    }
    Text("$start a ${end.minusDays(1)}")
    if (records.isEmpty()) Text("No hay registros en este período.")
    records.forEach { record ->
        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("${record.date} · ${record.name}", style = MaterialTheme.typography.titleMedium)
            Text(if (record.kind == "meal") "Realizada · ${record.primary}" else
                "${record.minutes / 60} h ${record.minutes % 60} min · ${record.primary}${record.secondary?.let { " + $it" } ?: ""}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onEdit(record) }) { Text("Editar") }
                OutlinedButton(onClick = { deleting = record.id }) { Text("Eliminar") }
            }
            if (deleting == record.id) {
                Text("¿Eliminar este registro? Solo se permite durante los primeros cinco minutos del ciclo actual.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        repository.mutate("delete", record, null, null, null, null) { result ->
                            result.onSuccess { onMessage("Registro eliminado"); deleting = null }
                                .onFailure { onError(it.message ?: "No se pudo eliminar"); deleting = null }
                        }
                    }) { Text("Confirmar") }
                    OutlinedButton(onClick = { deleting = null }) { Text("Cancelar") }
                }
            }
        }
    }
}
