package monkey.util;

import java.io.Serializable;

import util.LayoutComparison;

/**
 * Layout class represents the layout.
 * Currently, it is the content of the XML file that describes a page.
 * 
 * @author echo
 */
public class Layout implements Serializable {
	public Layout(String _activity, String _layoutContent) {
		assert _activity != null;
		assert _layoutContent != null;
		activity = _activity;
		// Remove the ignorable attributes in the layout XML 
		layoutContent = LayoutComparison.removeIgnorableAttributes(_layoutContent);
	}
	
	public String getLayoutContent() {
		return layoutContent;
	}
	
	public String getActivity() {
		return activity;
	}

	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null)
			return false;
		if(! this.getClass().equals(o.getClass()))
			return false;
		Layout layout = (Layout) o;
		return activity.equals(layout.activity) && 
				! LayoutComparison.hasDiff(this, layout);
	}
	
	/**
	 * Because the XML comparison applies special difference evaluator. 
	 * The hashCode() method also needs to apply the same difference evaluator 
	 * so that the hash code of two equally XML strings are the same. 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + activity.hashCode();
		result = prime * result + layoutContent.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Activity: ");
		builder.append(activity);
		builder.append(" Layout: ");
		builder.append(layoutContent);
		return builder.toString();
	}
	
	private String activity;
	private String layoutContent;
}
