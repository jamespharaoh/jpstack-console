package wbs.console.part;

import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.etc.Misc.doNothing;

import java.util.Set;

import lombok.NonNull;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;

import wbs.framework.database.Transaction;

import wbs.utils.string.FormatWriter;

public
interface PagePart {

	default
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	default
	Set <ScriptRef> scriptRefs () {

		return emptySet ();

	}

	default
	Set <HtmlLink> links () {

		return emptySet ();

	}

	default
	void renderHtmlHeadContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		doNothing ();

	}

	default
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		doNothing ();

	}

	default
	void setWithMarkup (
			boolean markup) {

		doNothing ();

	}

	default
	void cleanup () {

		doNothing ();

	}

}
