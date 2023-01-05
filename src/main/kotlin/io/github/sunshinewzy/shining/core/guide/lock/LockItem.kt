package io.github.sunshinewzy.shining.core.guide.lock

import io.github.sunshinewzy.shining.core.guide.ElementLock
import io.github.sunshinewzy.shining.core.guide.ShiningGuide
import io.github.sunshinewzy.shining.objects.item.SunSTIcon
import io.github.sunshinewzy.shining.utils.containsItem
import io.github.sunshinewzy.shining.utils.removeSItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getName
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic

class LockItem(
    var item: ItemStack,
    isConsume: Boolean = true
) : ElementLock({ player -> "&e物品 ${item.getName(player)} x${item.amount}" }, isConsume) {

    override fun check(player: Player): Boolean =
        player.inventory.containsItem(item)

    override fun consume(player: Player) {
        player.inventory.removeSItem(item)
    }

    override fun tip(player: Player) {
        player.openMenu<Basic>(ShiningGuide.TITLE) { 
            rows(3)
            
            map(
                "#",
                "ooooa"
            )
            
            set('#', SunSTIcon.BACK.item)
            set('a', item)
            
            onClick('#', ShiningGuide.onClickBack)
            
            onClick(lock = true)
        }
    }
}