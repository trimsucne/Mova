package co.edu.unal.dadm.mova.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object MovaIcons {
    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 20f)
                lineTo(10f, 14f)
                lineTo(14f, 14f)
                lineTo(14f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 11f)
                lineTo(22f, 11f)
                lineTo(12f, 2f)
                lineTo(2f, 11f)
                lineTo(5f, 11f)
                lineTo(5f, 20f)
                close()
            }
        }.build()
    }

    val Person: ImageVector by lazy {
        ImageVector.Builder(
            name = "Person",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 12f)
                curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
                curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
                curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
                curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
                close()
                moveTo(12f, 14f)
                curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
                lineTo(4f, 20f)
                lineTo(20f, 20f)
                lineTo(20f, 18f)
                curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
                close()
            }
        }.build()
    }

    val Star: ImageVector by lazy {
        ImageVector.Builder(
            name = "Star",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineTo(16.54f, 13.97f)
                lineTo(22f, 9.24f)
                lineTo(14.81f, 8.63f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.63f)
                lineTo(2f, 9.24f)
                lineTo(7.46f, 13.97f)
                lineTo(5.82f, 21f)
                close()
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 14f)
                lineTo(14.71f, 14f)
                lineTo(14.43f, 13.73f)
                curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
                curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
                curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
                curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
                lineTo(14f, 14.71f)
                lineTo(14f, 15.5f)
                lineTo(19f, 20.49f)
                lineTo(20.49f, 19f)
                lineTo(15.5f, 14f)
                close()
                moveTo(9.5f, 14f)
                curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
                curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
                curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
                close()
            }
        }.build()
    }

    val Bell: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 22f)
                curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
                lineTo(10f, 20f)
                curveTo(10f, 21.1f, 10.9f, 22f, 12f, 22f)
                close()
                moveTo(18f, 16f)
                lineTo(18f, 11f)
                curveTo(18f, 7.93f, 16.37f, 5.36f, 13.5f, 4.68f)
                lineTo(13.5f, 4f)
                curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
                curveTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f)
                lineTo(10.5f, 4.68f)
                curveTo(7.64f, 5.36f, 6f, 7.92f, 6f, 11f)
                lineTo(6f, 16f)
                lineTo(4f, 18f)
                lineTo(4f, 19f)
                lineTo(20f, 19f)
                lineTo(20f, 18f)
                lineTo(18f, 16f)
                close()
            }
        }.build()
    }

    val WaterDrop: ImageVector by lazy {
        ImageVector.Builder(
            name = "WaterDrop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.69f)
                lineTo(6.64f, 9.69f)
                curveTo(4.3f, 12.78f, 4.93f, 17.15f, 8.02f, 19.49f)
                curveTo(11.11f, 21.83f, 15.48f, 21.2f, 17.82f, 18.11f)
                curveTo(19.86f, 15.41f, 19.86f, 11.69f, 17.82f, 8.99f)
                lineTo(12f, 2.69f)
                close()
            }
        }.build()
    }

    val Flame: ImageVector by lazy {
        ImageVector.Builder(
            name = "Flame",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(12f, 2f, 6f, 7f, 6f, 13f)
                curveTo(6f, 16.31f, 8.69f, 19f, 12f, 19f)
                curveTo(15.31f, 19f, 18f, 16.31f, 18f, 13f)
                curveTo(18f, 7f, 12f, 2f, 12f, 2f)
                close()
            }
        }.build()
    }

    val Dumbbell: ImageVector by lazy {
        ImageVector.Builder(
            name = "Dumbbell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.57f, 14.86f)
                lineTo(18f, 12.29f)
                lineTo(16.57f, 13.71f)
                lineTo(10.29f, 7.43f)
                lineTo(11.71f, 6f)
                lineTo(9.14f, 3.43f)
                lineTo(7.71f, 4.86f)
                lineTo(4.86f, 2f)
                lineTo(2f, 4.86f)
                lineTo(4.86f, 7.71f)
                lineTo(3.43f, 9.14f)
                lineTo(6f, 11.71f)
                lineTo(7.43f, 10.29f)
                lineTo(13.71f, 16.57f)
                lineTo(12.29f, 18f)
                lineTo(14.86f, 20.57f)
                lineTo(16.29f, 19.14f)
                lineTo(19.14f, 22f)
                lineTo(22f, 19.14f)
                lineTo(19.14f, 16.29f)
                lineTo(20.57f, 14.86f)
                close()
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(13f, 17f)
                lineTo(11f, 17f)
                lineTo(11f, 11f)
                lineTo(13f, 11f)
                lineTo(13f, 17f)
                close()
                moveTo(13f, 9f)
                lineTo(11f, 9f)
                lineTo(11f, 7f)
                lineTo(13f, 7f)
                lineTo(13f, 9f)
                close()
            }
        }.build()
    }

}
