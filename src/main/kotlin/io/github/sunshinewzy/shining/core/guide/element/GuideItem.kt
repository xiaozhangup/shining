package io.github.sunshinewzy.shining.core.guide.element

import io.github.sunshinewzy.shining.api.guide.ElementDescription
import io.github.sunshinewzy.shining.api.guide.GuideContext
import io.github.sunshinewzy.shining.api.guide.state.IGuideElementState
import io.github.sunshinewzy.shining.api.namespace.NamespacedId
import io.github.sunshinewzy.shining.core.guide.GuideTeam
import io.github.sunshinewzy.shining.core.guide.state.GuideItemState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

open class GuideItem : GuideElement {

    constructor(id: NamespacedId, description: ElementDescription, item: ItemStack) : super(id, description, item)
    
    constructor() : super()
    

    override fun openMenu(player: Player, team: GuideTeam, context: GuideContext) {
        TODO("Not yet implemented")
    }

    override fun getState(): IGuideElementState {
        TODO("Not yet implemented")
    }

    override fun update(state: IGuideElementState, isMerge: Boolean): Boolean {
        if (state !is GuideItemState) return false
        if (!super.update(state, isMerge)) return false
        
        
        
        return true
    }

    override fun register(): GuideItem = super.register() as GuideItem
    
}