package io.github.sunshinewzy.shining.core.guide

import io.github.sunshinewzy.shining.Shining
import io.github.sunshinewzy.shining.api.guide.context.GuideContext
import io.github.sunshinewzy.shining.api.guide.element.IGuideElement
import io.github.sunshinewzy.shining.api.guide.element.IGuideElementContainer
import io.github.sunshinewzy.shining.api.guide.state.IGuideElementContainerState
import io.github.sunshinewzy.shining.api.guide.state.IGuideElementState
import io.github.sunshinewzy.shining.api.guide.team.IGuideTeam
import io.github.sunshinewzy.shining.api.lang.item.ILanguageItem
import io.github.sunshinewzy.shining.api.namespace.NamespacedId
import io.github.sunshinewzy.shining.api.objects.SPair
import io.github.sunshinewzy.shining.core.guide.context.AbstractGuideContextElement
import io.github.sunshinewzy.shining.core.guide.context.GuideEditorContext
import io.github.sunshinewzy.shining.core.guide.draft.GuideDraftContext
import io.github.sunshinewzy.shining.core.guide.draft.ShiningGuideDraft
import io.github.sunshinewzy.shining.core.guide.element.GuideElementRegistry
import io.github.sunshinewzy.shining.core.guide.state.GuideElementStateRegistry
import io.github.sunshinewzy.shining.core.lang.getLangText
import io.github.sunshinewzy.shining.core.lang.item.LanguageItem
import io.github.sunshinewzy.shining.core.lang.item.NamespacedIdItem
import io.github.sunshinewzy.shining.core.lang.sendPrefixedLangText
import io.github.sunshinewzy.shining.core.menu.onBack
import io.github.sunshinewzy.shining.core.menu.onBackMenu
import io.github.sunshinewzy.shining.core.menu.openMultiPageMenu
import io.github.sunshinewzy.shining.objects.ShiningDispatchers
import io.github.sunshinewzy.shining.objects.item.ShiningIcon
import io.github.sunshinewzy.shining.utils.orderWith
import org.bukkit.Material
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import java.util.*

object ShiningGuideEditor {

    private val editModeMap: MutableMap<UUID, Boolean> = HashMap()
    private val editorMap: MutableMap<UUID, Boolean> = HashMap()

    private val itemEditor = NamespacedIdItem(Material.COMPARATOR, NamespacedId(Shining, "shining_guide-editor"))
    private val itemCreateStateCopy = NamespacedIdItem(Material.REDSTONE_LAMP, NamespacedId(Shining, "shining_guide-editor-create_state_copy"))
    private val itemCreateStateNew = NamespacedIdItem(Material.GLASS, NamespacedId(Shining, "shining_guide-editor-create_state_new"))
    private val itemLoadFromDraftBox = NamespacedIdItem(Material.BOOKSHELF, NamespacedId(Shining, "shining_guide-editor-load_from_draft_box"))
    

    @JvmOverloads
    fun openEditor(
        player: Player,
        team: IGuideTeam,
        context: GuideContext,
        element: IGuideElement?,
        elementContainer: IGuideElementContainer? = null,
        elementContainerState: IGuideElementContainerState? = null
    ) {
        player.openMenu<Basic>(player.getLangText("menu-shining_guide-editor-title")) {
            rows(4)

            map(
                "-B-------",
                "-  a b  -",
                "- c p d -",
                "---------"
            )

            set('-', ShiningIcon.EDGE.item)

            context[GuideEditorContext.Back]?.let { 
                onBack(player) { it.onBack(this) }
            } ?: onBackMenu(player, team)
            
            fun back(event: ClickEvent) {
                context[GuideEditorContext.Back]?.let {
                    it.onBack(event)
                } ?: kotlin.run {
                    if (event.clickEvent().isShiftClick) {
                        ShiningGuide.openMainMenu(player, team, context)
                    } else {
                        ShiningGuide.openLastElement(player, team, context)
                    }
                }
            }

            if (element != null) {
                set('a', itemCreateStateCopy.toLocalizedItem(player)) {
                    val state = element.getState()
                    if (elementContainer != null) {
                        state.openEditor(player, team, GuideEditorContext.Update(elementContainer))
                    } else state.openEditor(player, team)
                }
            }
            
            if (elementContainer != null) {
                if (element != null) {
                    set('c', ShiningIcon.CUT.toLocalizedItem(player)) {
                        ShiningGuideClipboard.copy(player, GuideClipboardSession(element, elementContainer, GuideClipboardSession.Mode.CUT))
                        back(this)
                    }
                } else {
                    context[CreateContext]?.let { ctxt ->
                        if (ShiningGuideClipboard.hasClipboard(player)) {
                            set('p', ShiningIcon.PASTE.toLocalizedItem(player)) {
                                val session = ShiningGuideClipboard.paste(player) ?: return@set
                                ctxt.onCreate(session.element)
                                ShiningDispatchers.launchDB { 
                                    GuideElementRegistry.saveElement(elementContainer)
                                }
                                back(this)
                            }
                        }
                    }
                }
            }

            set('b', itemCreateStateNew.toLocalizedItem(player)) {
                openCreateNewStateEditor(player, team, context, element, elementContainer, elementContainerState)
            }
            
            set('d', itemLoadFromDraftBox.toLocalizedItem(player)) {
                ShiningGuideDraft.openLastSelectMenu(player, context + GuideDraftContext.Load(team, context, element, elementContainer, elementContainerState))
            }
            
            onClick(lock = true)
        }
    }
    
    private fun openCreateNewStateEditor(
        player: Player,
        team: IGuideTeam,
        context: GuideContext,
        element: IGuideElement?,
        elementContainer: IGuideElementContainer?,
        elementContainerState: IGuideElementContainerState?
    ) {
        player.openMultiPageMenu<SPair<Class<out IGuideElementState>, ILanguageItem>>(player.getLangText("menu-shining_guide-editor-create_new_state-title")) { 
            elements { GuideElementStateRegistry.getRegisteredClassPairList() }
            
            onGenerate { _, element, _, _ -> 
                element.second.toLocalizedItemStack(player)
            }
            
            onClick { _, element -> 
                val state = element.first.getConstructor().newInstance()
                state.openEditor(player, context = GuideEditorContext.Back {
                    submit(delay = 2) {
                        context[GuideEditorContext.Back]?.let {
                            it.onBack(this@Back)
                        } ?: openCreateNewStateEditor(player, team, context, elementContainer, elementContainer, elementContainerState)
                    }
                    
                    context[CreateContext]?.let { ctxtCreate ->
                        if (elementContainer != null) {
                            val theElement = state.toElement()
                            ShiningDispatchers.launchDB {
                                if (GuideElementRegistry.saveElement(theElement, true)) {
                                    submit {
                                        ctxtCreate.onCreate(theElement)
                                        ShiningDispatchers.launchDB {
                                            if (GuideElementRegistry.saveElement(elementContainer))
                                                player.sendPrefixedLangText("text-shining_guide-draft-load-success")
                                            else player.sendPrefixedLangText("text-shining_guide-draft-load-failure-save_container")
                                        }
                                    }
                                } else player.sendPrefixedLangText("text-shining_guide-draft-load-failure-save_element")
                            }
                        } else if (elementContainerState != null) {
                            val theElement = state.toElement()
                            ShiningDispatchers.launchDB {
                                if (GuideElementRegistry.saveElement(theElement, true)) {
                                    submit {
                                        ctxtCreate.onCreate(theElement)
                                        player.sendPrefixedLangText("text-shining_guide-draft-load-success")
                                    }
                                } else player.sendPrefixedLangText("text-shining_guide-draft-load-failure-save_element")
                            }
                        }
                    }
                })
            }

            onBack(player) {
                openEditor(player, team, context, element, elementContainer, elementContainerState)
            }
        }
    }
    

    fun isEditModeEnabled(player: Player): Boolean =
        editModeMap.getOrDefault(player.uniqueId, false)

    fun switchEditMode(player: Player): Boolean =
        (!isEditModeEnabled(player)).also {
            editModeMap[player.uniqueId] = it
        }
    
    fun isEditorEnabled(player: Player): Boolean =
        editorMap.getOrDefault(player.uniqueId, false)
    
    fun switchEditor(player: Player): Boolean =
        (!isEditorEnabled(player)).also { 
            editorMap[player.uniqueId] = it
        }
    
    fun isEditModeAndEditorEnabled(player: Player): Boolean =
        isEditModeEnabled(player) && isEditorEnabled(player)
        

    fun Basic.setEditor(
        player: Player,
        slot: Int = 6 orderWith 1,
        item: LanguageItem = itemEditor,
        onClick: ClickEvent.() -> Unit = {}
    ) {
        if (!isEditModeEnabled(player)) return
        
        set(
            slot,
            if (isEditorEnabled(player)) item.toStateItem("open").shiny().toLocalizedItem(player)
            else item.toStateItem("close").toLocalizedItem(player)
        ) {
            switchEditor(player)
            onClick(this)
        }
    }
    
    
    class CreateContext(val onCreate: (IGuideElement) -> Unit) : AbstractGuideContextElement(CreateContext) {
        companion object : GuideContext.Key<CreateContext>
    }

}