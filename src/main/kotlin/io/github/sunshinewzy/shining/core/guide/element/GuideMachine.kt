package io.github.sunshinewzy.shining.core.guide.element

import io.github.sunshinewzy.shining.api.namespace.NamespacedId
import io.github.sunshinewzy.shining.core.guide.GuideElement
import io.github.sunshinewzy.shining.core.guide.GuideTeam
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GuideMachine(id: NamespacedId, symbol: ItemStack) : GuideElement(id, symbol) {

    
    override fun openAction(player: Player, team: GuideTeam) {
        
    }
    
}