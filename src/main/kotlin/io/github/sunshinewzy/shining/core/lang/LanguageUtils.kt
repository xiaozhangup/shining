package io.github.sunshinewzy.shining.core.lang

import io.github.sunshinewzy.shining.api.ShiningConfig.language
import io.github.sunshinewzy.shining.api.addon.ShiningAddon
import io.github.sunshinewzy.shining.api.namespace.NamespacedId
import io.github.sunshinewzy.shining.core.lang.node.LanguageNode
import io.github.sunshinewzy.shining.core.lang.node.ListNode
import io.github.sunshinewzy.shining.core.lang.node.SectionNode
import io.github.sunshinewzy.shining.core.lang.node.TextNode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun NamespacedId.toNodeString(): String =
    "${namespace.name}-$id"

fun NamespacedId.toNodeString(prefix: String): String =
    if(prefix == "") toNodeString() else "$prefix-${toNodeString()}"

@JvmOverloads
fun NamespacedId.getLanguageNodeOrNull(prefix: String = "", locale: String = language): LanguageNode? =
    ShiningLanguageManager.getLanguageNode(this, prefix, locale)

@JvmOverloads
fun NamespacedId.getLanguageNode(prefix: String = "", locale: String = language): LanguageNode =
    getLanguageNodeOrNull(prefix, locale) ?: throw LanguageException("Cannot find the node '${toNodeString(prefix)}' in '$locale.yml'.")


fun getDefaultLanguageNode(node: String): LanguageNode? =
    ShiningLanguageManager.getLanguageNode(language, node)

fun getDefaultLangTextNode(node: String): TextNode? =
    ShiningLanguageManager.getLangTextNode(language, node)

fun getDefaultLangListNode(node: String): ListNode? =
    ShiningLanguageManager.getLangListNode(language, node)

fun getDefaultLangSectionNode(node: String): SectionNode? =
    ShiningLanguageManager.getLangSectionNode(language, node)

fun getDefaultLangTextOrNull(node: String): String? =
    ShiningLanguageManager.getLangTextOrNull(language, node)

fun getDefaultLangText(node: String): String =
    ShiningLanguageManager.getLangText(language, node)


fun getDefaultLanguageNode(addon: ShiningAddon, node: String): LanguageNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLanguageNode(language, node)

fun getDefaultLangTextNode(addon: ShiningAddon, node: String): TextNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangTextNode(language, node)

fun getDefaultLangListNode(addon: ShiningAddon, node: String): ListNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangListNode(language, node)

fun getDefaultLangSectionNode(addon: ShiningAddon, node: String): SectionNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangSectionNode(language, node)

fun getDefaultLangTextOrNull(addon: ShiningAddon, node: String): String? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangTextOrNull(language, node)

fun getDefaultLangText(addon: ShiningAddon, node: String): String =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangText(language, node)
        ?: throw LanguageException("Cannot find the language manager of the addon '${addon.getName()}'.")


fun CommandSender.getLocale(): String =
    if(this is Player) locale else language

fun CommandSender.getLanguageNode(node: String): LanguageNode? =
    ShiningLanguageManager.getLanguageNode(getLocale(), node)

fun CommandSender.getLangTextNode(node: String): TextNode? =
    ShiningLanguageManager.getLangTextNode(getLocale(), node)

fun CommandSender.getLangListNode(node: String): ListNode? =
    ShiningLanguageManager.getLangListNode(getLocale(), node)

fun CommandSender.getLangSectionNode(node: String): SectionNode? =
    ShiningLanguageManager.getLangSectionNode(getLocale(), node)

fun CommandSender.getLangTextOrNull(node: String): String? =
    ShiningLanguageManager.getLangTextOrNull(getLocale(), node)

fun CommandSender.getLangText(node: String): String =
    ShiningLanguageManager.getLangText(getLocale(), node)

fun CommandSender.sendLangText(node: String): Boolean =
    getLangTextOrNull(node)?.let { 
        sendMessage(it)
        true
    } ?: false


fun CommandSender.getLanguageNode(addon: ShiningAddon, node: String): LanguageNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLanguageNode(getLocale(), node)

fun CommandSender.getLangTextNode(addon: ShiningAddon, node: String): TextNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangTextNode(getLocale(), node)

fun CommandSender.getLangListNode(addon: ShiningAddon, node: String): ListNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangListNode(getLocale(), node)

fun CommandSender.getLangSectionNode(addon: ShiningAddon, node: String): SectionNode? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangSectionNode(getLocale(), node)

fun CommandSender.getLangTextOrNull(addon: ShiningAddon, node: String): String? =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangTextOrNull(getLocale(), node)

fun CommandSender.getLangText(addon: ShiningAddon, node: String): String =
    ShiningLanguageManager.getAddonLanguageManager(addon)?.getLangText(getLocale(), node)
        ?: throw LanguageException("Cannot find the language manager of the addon '${addon.getName()}'.")

fun CommandSender.sendLangText(addon: ShiningAddon, node: String): Boolean =
    getLangTextOrNull(addon, node)?.let {
        sendMessage(it)
        true
    } ?: false