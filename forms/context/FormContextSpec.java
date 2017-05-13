package wbs.console.forms.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("form-context")
@PrototypeComponent ("formContextSpec")
public
class FormContextSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataParent
	FormContextsSpec formContexts;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "class",
		required = true)
	String className;

	@DataAttribute (
		name = "type",
		required = true)
	FormType formType;

	@DataAttribute (
		name = "column-fields",
		required = true)
	String columnFieldsName;

	@DataAttribute (
		name = "row-fields")
	String rowFieldsName;

}
