package io.github.sunshinewzy.shining.core

import io.github.sunshinewzy.shining.api.IClassRegistry
import io.github.sunshinewzy.shining.api.IShiningAPI
import io.github.sunshinewzy.shining.api.guide.element.IGuideElementRegistry
import io.github.sunshinewzy.shining.api.guide.reward.IGuideReward
import io.github.sunshinewzy.shining.api.guide.state.IGuideElementState
import io.github.sunshinewzy.shining.api.item.universal.UniversalItem
import io.github.sunshinewzy.shining.core.guide.element.GuideElementRegistry
import io.github.sunshinewzy.shining.core.guide.reward.GuideRewardRegistry
import io.github.sunshinewzy.shining.core.guide.state.GuideElementStateRegistry
import io.github.sunshinewzy.shining.core.item.universal.UniversalItemRegistry

class ShiningAPI : IShiningAPI {

    override fun getUniversalItemRegistry(): IClassRegistry<UniversalItem> = UniversalItemRegistry

    override fun getGuideElementRegistry(): IGuideElementRegistry = GuideElementRegistry
    
    override fun getGuideElementStateRegistry(): IClassRegistry<IGuideElementState> = GuideElementStateRegistry

    override fun getGuideRewardRegistry(): IClassRegistry<IGuideReward> = GuideRewardRegistry
    
}