package util;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEngine;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.util.Predicate;

import monkey.util.Layout;

/**
 * This class compares two XML files.
 * @author echo
 */
public class LayoutComparison {
	private static DifferenceEngine diffEngine;
	// A lambda expression, which is a listener once difference is found.
	private static ComparisonListener comparisonListener;
	// A lambda expression, which defines the comparison. 
	// It is used to ignore some attributes in the XML file during comparison.
	private static DifferenceEvaluator diffEvaluator;
	// A lambda Expression, which is used to filter out the attributes that are not considered during comparison.
	private static Predicate<Attr> attrFilter;
	// A set that contains the name of the attributes which are ignorable during comparison.
	private final static Set<String> ignorableAttrs;

	static {
//		ignorableAttrs = new HashSet<>(Arrays.asList(
//				"focused"));
		ignorableAttrs = new HashSet<>();

		diffEngine = new DOMDifferenceEngine();

		comparisonListener = (comparison, result) -> {
			Log.println("Difference found: " + comparison);
		};

		diffEngine.addDifferenceListener(comparisonListener);

		diffEvaluator = (comparison, outcome) -> {
			if(outcome == ComparisonResult.EQUAL)
				return ComparisonResult.EQUAL;
			final Node controlNode = comparison.getControlDetails().getTarget();
			if(controlNode instanceof Attr) {
				Attr attr = (Attr) controlNode;
				// apply ignorable XML attributes
				if(ignorableAttrs.contains(attr.getName()))
					return ComparisonResult.EQUAL;
			}
			return outcome;
		};

		attrFilter = (attr) -> {
			if(ignorableAttrs.contains(attr.getName()))
				return false;
			else return true;
		};
	}

	/**
	 * Compare two XML files denoted by a and b.
	 * If one of a and b is null, skip comparison.
	 */
	@Deprecated
	public static void diff(Object a, Object b) {
		if(a == null || b == null)
			return;
		diffEngine.compare(Input.from(a).build(), Input.from(b).build());
	}

	/**
	 * Obtain the differences between two XML files denoted by a and b.
	 * This method defines whether two XML files are the same. 
	 * Some labels or attributes in the XML files can be ignored during comparison, 
	 * like the color of text view.
	 * 
	 * TODO
	 * Ignore some labels or attributes during comparison.
	 */
	public static Diff getDiff(String a, String b) {
		if(a == null || b == null)
			return null;
		Diff diff = DiffBuilder.compare(Input.from(a))
				.withTest(Input.from(b))
				.checkForIdentical()
				.ignoreComments()
				.ignoreWhitespace()
				.normalizeWhitespace()
				// apply the difference evaluator
				// .withDifferenceEvaluator(diffEvaluator)
				.withAttributeFilter(attrFilter)
				.build();
		return diff;
	}
	
	public static Diff getDiff(Layout a, Layout b) {
		return getDiff(a.getLayoutContent(), b.getLayoutContent());
	}

	/**
	 * Return whether there is difference between two XML files denoted by a and b.
	 */
	public static boolean hasDiff(String a, String b) {
		Diff diff = getDiff(a, b);
		if(diff == null)
			return true;
		return getDiff(a, b).hasDifferences();
	}
	
	public static boolean hasDiff(Layout a, Layout b) {
		return hasDiff(a.getLayoutContent(), b.getLayoutContent());
	}

	// Remove the ignorable attributes from the XML and serialize it back to string. 
	public static String removeIgnorableAttributes(String xml) {
		return removeIgnorableAttributes(xml, ignorableAttrs);
	}
	
	public static String removeIgnorableAttributes(String xml, Collection<String> _ignorableAttrs) {
		Document document = parse(xml);
		// Find all the elements in the DOM
		NodeList nodeList = document.getElementsByTagName("*");
		for(int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				// Remove the ignorable attributes
				for(String ignorableAttr : _ignorableAttrs)
					if(element.hasAttribute(ignorableAttr))
						element.removeAttribute(ignorableAttr);
			}
		}
		// Serialize the XML back to string
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		return serializer.writeToString(document);
	}

	// Parse the xml string to DOM object.
	private static Document parse(String xml) {
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			Log.println("XML parsing error.");
			e.printStackTrace();
			System.exit(0);
		}
		return document;
	}
}