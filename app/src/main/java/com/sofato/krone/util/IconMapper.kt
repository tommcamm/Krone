package com.sofato.krone.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {

    private val iconMap = mapOf(
        "ShoppingCart" to Icons.Outlined.ShoppingCart,
        "Restaurant" to Icons.Outlined.Restaurant,
        "LocalCafe" to Icons.Outlined.LocalCafe,
        "DirectionsBus" to Icons.Outlined.DirectionsBus,
        "ShoppingBag" to Icons.Outlined.ShoppingBag,
        "TheaterComedy" to Icons.Outlined.TheaterComedy,
        "LocalPharmacy" to Icons.Outlined.LocalPharmacy,
        "CardGiftcard" to Icons.Outlined.CardGiftcard,
        "Home" to Icons.Outlined.Home,
        "MoreHoriz" to Icons.Outlined.MoreHoriz,
        "Bolt" to Icons.Outlined.Bolt,
        "Whatshot" to Icons.Outlined.Whatshot,
        "WaterDrop" to Icons.Outlined.WaterDrop,
        "Wifi" to Icons.Outlined.Wifi,
        "Shield" to Icons.Outlined.Shield,
        "WorkOutline" to Icons.Outlined.WorkOutline,
        "Subscriptions" to Icons.Outlined.Subscriptions,
        "AccountBalance" to Icons.Outlined.AccountBalance,
    )

    val availableIcons: Map<String, ImageVector> = iconMap

    fun getIcon(name: String): ImageVector =
        iconMap[name] ?: Icons.Outlined.Category
}
