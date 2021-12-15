package ui.external.materialui

import react.ComponentClass
import react.Props

@JsModule("@material-ui/icons")
@JsNonModule
private external val iconModule: dynamic

object Icons {
    val save = iconModule.SaveAlt.unsafeCast<ComponentClass<IconProps>>()
    val refresh = iconModule.Refresh.unsafeCast<ComponentClass<IconProps>>()
    val language = iconModule.Language.unsafeCast<ComponentClass<IconProps>>()
    val feedback = iconModule.Feedback.unsafeCast<ComponentClass<IconProps>>()
    val arrowBack = iconModule.ArrowBack.unsafeCast<ComponentClass<IconProps>>()
    val liveHelp = iconModule.LiveHelp.unsafeCast<ComponentClass<IconProps>>()
    val edit = iconModule.Edit.unsafeCast<ComponentClass<IconProps>>()
    val checkCircle = iconModule.CheckCircle.unsafeCast<ComponentClass<IconProps>>()
    val addCircle = iconModule.AddCircle.unsafeCast<ComponentClass<IconProps>>()
    val removeCircle = iconModule.RemoveCircle.unsafeCast<ComponentClass<IconProps>>()
    val replay = iconModule.Replay.unsafeCast<ComponentClass<IconProps>>()
    val check = iconModule.Check.unsafeCast<ComponentClass<IconProps>>()
    val settings = iconModule.Settings.unsafeCast<ComponentClass<IconProps>>()
    val fetch = iconModule.GetApp.unsafeCast<ComponentClass<IconProps>>()
    val publish = iconModule.Publish.unsafeCast<ComponentClass<IconProps>>()
}

external interface IconProps : Props {
    var color: String
    var style: Style
}
