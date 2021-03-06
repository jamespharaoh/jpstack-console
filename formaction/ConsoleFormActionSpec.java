package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("form-action")
@PrototypeComponent ("contextFormActionSpec")
public
class ConsoleFormActionSpec
	implements ConsoleSpec {

	@DataAttribute (
		required = true,
		format = StringFormat.hyphenated)
	String name;

	@DataAttribute (
		name = "action-form",
		format = StringFormat.hyphenated)
	String actionFormTypeName;

	@DataAttribute
	String helpText;

	@DataAttribute
	String submitLabel;

	@DataAttribute (
		name = "helper",
		format = StringFormat.camelCase)
	String helperName;

	@DataAttribute
	String historyHeading;

	@DataAttribute (
		name = "history-form",
		format = StringFormat.hyphenated)
	String historyFormTypeName;

}
