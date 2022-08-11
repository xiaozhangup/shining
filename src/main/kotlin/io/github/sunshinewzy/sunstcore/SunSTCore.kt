package io.github.sunshinewzy.sunstcore

import io.github.sunshinewzy.sunstcore.api.Namespace
import io.github.sunshinewzy.sunstcore.api.SPlugin
import io.github.sunshinewzy.sunstcore.core.data.DataManager
import io.github.sunshinewzy.sunstcore.core.data.internal.SLocationData
import io.github.sunshinewzy.sunstcore.core.guide.SGuide
import io.github.sunshinewzy.sunstcore.core.guide.element.GuideCategory
import io.github.sunshinewzy.sunstcore.core.guide.element.GuideItem
import io.github.sunshinewzy.sunstcore.core.guide.lock.LockExperience
import io.github.sunshinewzy.sunstcore.core.guide.lock.LockItem
import io.github.sunshinewzy.sunstcore.core.machine.legacy.*
import io.github.sunshinewzy.sunstcore.core.machine.legacy.custom.SMachineRecipe
import io.github.sunshinewzy.sunstcore.core.machine.legacy.custom.SMachineRecipes
import io.github.sunshinewzy.sunstcore.core.machine.manager.IMachineManager
import io.github.sunshinewzy.sunstcore.core.machine.manager.MachineManager
import io.github.sunshinewzy.sunstcore.core.task.TaskProgress
import io.github.sunshinewzy.sunstcore.listeners.SunSTSubscriber
import io.github.sunshinewzy.sunstcore.objects.SBlock
import io.github.sunshinewzy.sunstcore.objects.SItem
import io.github.sunshinewzy.sunstcore.objects.item.SunSTItem
import io.github.sunshinewzy.sunstcore.objects.machine.SunSTMachineManager
import io.github.sunshinewzy.sunstcore.utils.SReflect
import io.github.sunshinewzy.sunstcore.utils.SunSTTestApi
import io.github.sunshinewzy.sunstcore.utils.subscribeEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.PluginManager
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginVersion
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.metrics.Metrics
import taboolib.module.nms.nmsGeneric
import taboolib.platform.BukkitPlugin


@RuntimeDependencies(
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    ),
    RuntimeDependency(
        value = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3",
        relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
    )
)
object SunSTCore : Plugin(), SPlugin {
    const val NAME = "SunSTCore"
    const val COLOR_NAME = "§eSunSTCore"
    
    @Config
    lateinit var config: Configuration
        private set
    
    val plugin: BukkitPlugin by lazy { BukkitPlugin.getInstance() }
    val pluginManager: PluginManager by lazy { Bukkit.getPluginManager() }
    val prefixName: String by lazy { config.getString("prefix_name")?.colored() ?: COLOR_NAME }
    val machineManager: IMachineManager by lazy { MachineManager }
    
    private val namespace = Namespace.get(NAME.lowercase())
    
    
    override fun onEnable() {
        
        registerSerialization()
        registerListeners()
        init()
        
        val metrics = Metrics(10212, pluginVersion, Platform.BUKKIT)
        
        info("SunSTCore 加载成功！")
        
        if(System.getProperty("SunSTDebug") == "true")
            test()
    }

    override fun onDisable() {
        
    }

    override fun getName(): String {
        return NAME
    }

    override fun getNamespace(): Namespace {
        return namespace
    }
    

    private fun init() {
        try {
            SReflect.init()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        
        SItem.initAction()
        DataManager.init()
        SunSTItem.init()
        SMachineWrench.init()
        SLocationData.init()
        SSingleMachine.init()
        SFlatMachine.init()
        SunSTMachineManager.register()
        
        nmsGeneric
        
    }
    
    private fun registerListeners() {
        SunSTSubscriber.init()
    }
    
    private fun registerSerialization() {
        arrayOf(
            SBlock::class.java,
            TaskProgress::class.java,
            SMachineInformation::class.java, SSingleMachineInformation::class.java, SFlatMachineInformation::class.java,
            SMachineRecipe::class.java, SMachineRecipes::class.java
        ).forEach { 
            ConfigurationSerialization.registerClass(it)
        }
    }
    
    
    @SunSTTestApi
    private fun test() {
        
        val stoneCategory = GuideCategory("STONE_AGE", SItem(Material.STONE, "&f石器时代", "&d一切的起源"))
        val steamCategory = GuideCategory("STEAM_AGE", SItem(Material.IRON_INGOT, "&e蒸汽时代", "&d第一次工业革命"))
        val electricalCategory = GuideCategory("ELECTRICAL_AGE", SItem(Material.NETHERITE_INGOT, "&a电器时代", "&d第二次工业革命"))
        val informationCateGory = GuideCategory("INFORMATION_AGE", SItem(Material.DIAMOND, "&b信息时代", "&d技术爆炸"))
        
        steamCategory.registerDependency(stoneCategory)
        electricalCategory.registerDependency(steamCategory)
        informationCateGory.registerDependency(electricalCategory)
        
        val lockExperience = LockExperience(5)
        
        val newStoneCategory = GuideCategory("NEW_STONE_AGE", SItem(Material.STONE_BRICKS, "&a新石器时代"))
        val stickItem = GuideItem("STICK", SItem(Material.STICK, "&6工具的基石"))
        newStoneCategory.registerElement(stickItem)
        stoneCategory.registerElement(newStoneCategory)
        
        val oldStoneCategory = GuideCategory("OLD_STONE_AGE", SItem(Material.COBBLESTONE, "&7旧石器时代"))
        stoneCategory.registerElement(oldStoneCategory)
        
        stickItem.registerLock(lockExperience)
        newStoneCategory.registerLock(lockExperience)
        newStoneCategory.registerLock(LockItem(SItem(Material.SNOWBALL, 3)))
        oldStoneCategory.registerLock(LockExperience(10, false))
        
        SGuide.registerElement(electricalCategory, 12)
        SGuide.registerElement(stoneCategory)
        SGuide.registerElement(informationCateGory, 13)
        SGuide.registerElement(steamCategory, 11)
        
        
        subscribeEvent<PlayerInteractEvent>(ignoreCancelled = false) {
            if(hand != EquipmentSlot.HAND) return@subscribeEvent
            
            if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                val item = player.inventory.itemInMainHand
                when(item.type) {
                    Material.DIAMOND -> {
                        SGuide.openLastElement(player)
                    }
                    
                    Material.EMERALD -> {
                        SGuide.fireworkCongratulate(player)
                    }
                    
                    Material.AIR -> {}
                    
                    else -> {
                        
                    }
                }
                
            }
        }
    }
    
}