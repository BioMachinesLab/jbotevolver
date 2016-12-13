package gui;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.ViewportLayout;

public class ConstrainedViewPortLayout extends ViewportLayout {
	private static final long serialVersionUID = -7619031680550892640L;

	@Override
	public Dimension preferredLayoutSize(Container parent) {

		Dimension preferredViewSize = super.preferredLayoutSize(parent);

		Container viewportContainer = parent.getParent();
		if (viewportContainer != null) {
			Dimension parentSize = viewportContainer.getSize();
			preferredViewSize.height = parentSize.height;
		}

		return preferredViewSize;
	}
}