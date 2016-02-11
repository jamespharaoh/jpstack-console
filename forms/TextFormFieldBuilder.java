package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.utils.etc.BeanLogic;

@SuppressWarnings ({ "rawtypes", "unchecked" })
@PrototypeComponent ("textFormFieldBuilder")
@ConsoleModuleBuilderHandler
public
class TextFormFieldBuilder {

	// dependencies

	@Inject
	FormFieldPluginManagerImplementation formFieldPluginManager;

	// prototype dependencies

	@Inject
	Provider<DynamicFormFieldAccessor> dynamicFormFieldAccessorProvider;

	@Inject
	Provider<HiddenFormField> hiddenFormFieldProvider;

	@Inject
	Provider<IdentityFormFieldInterfaceMapping>
	identityFormFieldInterfaceMappingProvider;

	@Inject
	Provider<NullFormFieldConstraintValidator>
	nullFormFieldValueConstraintValidatorProvider;

	@Inject
	Provider<ReadOnlyFormField> readOnlyFormFieldProvider;

	@Inject
	Provider<RequiredFormFieldValueValidator>
	requiredFormFieldValueValidatorProvider;

	@Inject
	Provider<SimpleFormFieldAccessor> simpleFormFieldAccessorProvider;

	@Inject
	Provider<TextFormFieldRenderer> textFormFieldRendererProvider;

	@Inject
	Provider<UpdatableFormField> updatableFormFieldProvider;

	// builder

	@BuilderParent
	FormFieldBuilderContext context;

	@BuilderSource
	TextFormFieldSpec textFormFieldSpec;

	@BuilderTarget
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			textFormFieldSpec.name ();

		String label =
			ifNull (
				textFormFieldSpec.label (),
				capitalise (camelToSpaces (name)));

		Boolean readOnly =
			ifNull (
				textFormFieldSpec.readOnly (),
				false);

		Boolean hidden =
			ifNull (
				textFormFieldSpec.hidden (),
				false);

		Boolean nullable =
			ifNull (
				textFormFieldSpec.nullable (),
				false);

		Boolean dynamic =
			ifNull (
				textFormFieldSpec.dynamic (),
				false);

		// field type

		Class<?> propertyClass;

		if (! dynamic) {

			propertyClass =
				BeanLogic.propertyClassForClass (
					context.containerClass (),
					name);

		} else {

			propertyClass = String.class;

		}

		// accessor

		FormFieldAccessor accessor;

		if (dynamic) {

			accessor =
				dynamicFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

		} else {

			accessor =
				simpleFormFieldAccessorProvider.get ()

				.name (
					name)

				.nativeClass (
					propertyClass);

		}

		// TODO dynamic

		// native mapping

		FormFieldNativeMapping nativeMapping =
			formFieldPluginManager.getNativeMappingRequired (
				context,
				context.containerClass (),
				name,
				String.class,
				propertyClass);

		// value validator

		List<FormFieldValueValidator> valueValidators =
			new ArrayList<> ();

		if (! nullable) {

			valueValidators.add (
				requiredFormFieldValueValidatorProvider.get ());

		}

		// constraint validator

		FormFieldConstraintValidator constraintValidator =
			nullFormFieldValueConstraintValidatorProvider.get ();

		// interface mapping

		FormFieldInterfaceMapping interfaceMapping =
			identityFormFieldInterfaceMappingProvider.get ();

		// renderer

		FormFieldRenderer renderer =
			textFormFieldRendererProvider.get ()

			.name (
				name)

			.label (
				label)

			.nullable (
				nullable);

		// update hook

		FormFieldUpdateHook updateHook =
			formFieldPluginManager.getUpdateHook (
				context,
				context.containerClass (),
				name);

		// form field

		if (hidden) {

			formFieldSet.addFormField (
				hiddenFormFieldProvider.get ()

				.name (
					name)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.csvMapping (
					interfaceMapping)

				.implicitValue (
					Optional.absent ())

			);

		} else if (readOnly) {

			formFieldSet.addFormField (
				readOnlyFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.interfaceMapping (
					interfaceMapping)

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

			);

		} else {

			formFieldSet.addFormField (
				updatableFormFieldProvider.get ()

				.name (
					name)

				.label (
					label)

				.accessor (
					accessor)

				.nativeMapping (
					nativeMapping)

				.valueValidators (
					valueValidators)

				.constraintValidator (
					constraintValidator)

				.interfaceMapping (
					interfaceMapping)

				.csvMapping (
					interfaceMapping)

				.renderer (
					renderer)

				.updateHook (
					updateHook)

			);

		}

	}

}
