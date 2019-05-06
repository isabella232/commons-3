package io.onedev.commons.jsymbol.cpp.symbols;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.PackageResourceReference;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.jsymbol.cpp.symbols.ui.icon.IconLocator;
import io.onedev.commons.jsymbol.util.HighlightableLabel;
import io.onedev.commons.jsymbol.util.NoAntiCacheImage;
import io.onedev.commons.utils.LinearRange;
public class ClassSymbol extends CppSymbol {

	private static final long serialVersionUID = 1L;
	private boolean isTemp;
	private Modifier modifier;
	public ClassSymbol(CppSymbol parent, String name, boolean local, PlanarRange position, 
			PlanarRange scope,Modifier modifier, boolean isTemp) {
		super(parent, name, local, position, scope);
		this.modifier=modifier; 
		this.isTemp=isTemp;
	}
  
	@Override
	public boolean isPrimary() {
		return true;
	}

	@Override
	public Image renderIcon(String componentId) {
		String loadIcon="class_obj.png";
		switch(modifier){
		case NORMAL:
			loadIcon="class_obj.png";
			if(isTemp){
				loadIcon="template_obj.png";
			}
			break;
		case PRIVATE:
			loadIcon="pri_class_obj.png";
			if(isTemp){
				loadIcon="pri_temp_obj.png";
			}
			break;
		case PROTECTED:
			loadIcon="pro_class_obj.png";
			if(isTemp){
				loadIcon="pro_temp_obj.png";
			}
			break;
		case PUBLIC:
			loadIcon="class_obj.png";
			if(isTemp){
				loadIcon="template_obj.png";
			}
			break;
		case FRIENDCLASS:
			loadIcon="friend_class_obj.png";
			if(isTemp){
				loadIcon="friend_temp_obj.png";
			}
			break;
		default:
			break;
		}
		if(isTemp){
			loadIcon="template_obj.png";
		}
		Image icon = new NoAntiCacheImage(componentId, 
            		new PackageResourceReference(IconLocator.class, loadIcon));
        icon.add(AttributeAppender.append("title", "class"));
        return icon;
	}

	@Override
	public Component render(String componentId, LinearRange highlight) {
		return new HighlightableLabel(componentId, getName(), highlight);
	}

}

