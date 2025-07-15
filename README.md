App en Android: Recetario Movil Con Programación Reactiva
Desarrollado con emulador API 34


# 🍳 Recetario Reactivo Movil

Una aplicación de recetas moderna y elegante desarrollada con **Kotlin** y **Jetpack Compose**, diseñada para ofrecer una experiencia culinaria única y profesional.

## ✨ Características Principales

### 🎨 Diseño Profesional
- **Paleta de colores culinaria**: Inspirada en ingredientes naturales (tomate, albahaca, zanahoria, etc.)
- **Interfaz moderna**: Diseño Material 3 con componentes personalizados
- **Tarjetas atractivas**: Cada receta se presenta con imágenes profesionales y información detallada
- **Sin fondos**: Diseño limpio y minimalista como solicitado

### 🎤 Búsqueda por Voz
- **Reconocimiento de voz nativo**: Utiliza la API de Android Speech Recognition
- **Búsqueda inteligente**: Pregunta sobre recetas disponibles y obtén resultados instantáneos
- **Interfaz intuitiva**: Diálogo interactivo con indicadores visuales
- **Manejo de errores**: Gestión robusta de errores de reconocimiento

### 📱 Funcionalidades Avanzadas
- **Navegación fluida**: Sistema de navegación optimizado con NavHost
- **Gestión de favoritos**: Marca y gestiona tus recetas favoritas
- **Receta aleatoria**: Descubre nuevas recetas con un toque
- **Ajuste de porciones**: Calcula automáticamente las cantidades según el número de personas
- **Checklist de ingredientes**: Marca los ingredientes que ya tienes

### 🗄️ Base de Datos Robusta
- **Room Database**: Almacenamiento local eficiente
- **Migraciones automáticas**: Actualizaciones de esquema sin pérdida de datos
- **Datos de ejemplo**: Recetas predefinidas para empezar a usar la app

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje principal
- **Jetpack Compose**: UI declarativa moderna
- **Room Database**: Persistencia de datos
- **ViewModel & LiveData**: Gestión de estado
- **Navigation Component**: Navegación entre pantallas
- **Coil**: Carga de imágenes
- **Speech Recognition**: API de reconocimiento de voz nativa

## 🎯 Funcionalidades de Voz

### Comandos de Voz Soportados
- "Buscar recetas de pollo"
- "Mostrar postres"
- "Recetas fáciles"
- "Comida vegetariana"
- "Recetas rápidas"
- "Platos principales"

### Cómo Usar la Búsqueda por Voz
1. Toca el botón de micrófono en la barra de búsqueda
2. Di tu consulta claramente
3. La app procesará tu voz y mostrará los resultados
4. Toca "Buscar" para aplicar la búsqueda

## 🎨 Paleta de Colores

### Colores Principales
- **Tomate Red** (#E74C3C): Color principal
- **Basil Green** (#27AE60): Color secundario
- **Carrot Orange** (#E67E22): Color terciario
- **Butter Yellow** (#F1C40F): Acentos
- **Cream White** (#FDFEFE): Superficies claras
- **Charcoal Gray** (#2C3E50): Textos y elementos oscuros

### Colores por Categoría
- **Desayuno**: Amarillo mantequilla
- **Almuerzo**: Verde albahaca
- **Cena**: Azul océano
- **Postre**: Púrpura lavanda
- **Snack**: Naranja zanahoria
- **Bebida**: Marrón cacao

## 📱 Pantallas Principales

### 1. Pantalla Principal (Explorar)
- Lista de recetas con diseño de tarjetas
- Barra de búsqueda con botón de voz
- Filtros por categoría
- Botones flotantes para receta aleatoria y agregar

### 2. Detalle de Receta
- Imagen de portada con overlay
- Información nutricional y tiempo
- Slider de porciones interactivo
- Lista de ingredientes con checklist
- Pasos de preparación expandibles

### 3. Favoritos
- Vista dedicada a recetas favoritas
- Estado vacío informativo
- Navegación directa a detalles

### 4. Agregar/Editar Receta
- Formulario completo para crear recetas
- Validación de datos
- Interfaz intuitiva

## 🔧 Configuración del Proyecto

### Requisitos
- Android Studio Hedgehog o superior
- Android SDK 34
- Kotlin 1.9.0+

### Instalación
1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza las dependencias de Gradle
4. Ejecuta la aplicación en un dispositivo o emulador

### Permisos Requeridos
- `RECORD_AUDIO`: Para reconocimiento de voz
- `INTERNET`: Para funcionalidades de red

## 🚀 Características Técnicas

### Arquitectura
- **MVVM**: Model-View-ViewModel
- **Repository Pattern**: Abstracción de datos
- **Dependency Injection**: Gestión de dependencias
- **Coroutines**: Programación asíncrona

### Base de Datos
- **Room**: ORM para Android
- **Migraciones**: Control de versiones de esquema
- **Type Converters**: Conversión de tipos complejos

### UI/UX
- **Material 3**: Diseño moderno
- **Dark/Light Theme**: Soporte para temas
- **Responsive Design**: Adaptable a diferentes pantallas
- **Accessibility**: Soporte para accesibilidad

## 📸 Capturas de Pantalla

*[Aquí se pueden agregar capturas de pantalla de la aplicación]*

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor, asegúrate de:
1. Fork el proyecto
2. Crear una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍🍳 Autor

Desarrollado con ❤️ para amantes de la cocina.

---

**¡Disfruta cocinando con Recetario Gourmet!** 🍽️ 
