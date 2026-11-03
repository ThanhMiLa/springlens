package vn.io.codelearning.springapitester.ui;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

final class ResponsiveEndpointToolbar extends JPanel {

    private enum DisplayMode {
        FULL, RELOAD_AND_CLEAR_MENU, ACTIONS_MENU, COMPACT
    }

    private final JComboBox<?> fileFilter;
    private final List<JButton> actionButtons;
    private final JButton overflowButton = new JButton("▾");
    private JPopupMenu overflowMenu;
    private DisplayMode displayMode = DisplayMode.FULL;

    ResponsiveEndpointToolbar(JComboBox<?> fileFilter, JButton addButton, JButton reloadButton, JButton clearButton) {
        this.fileFilter = fileFilter;
        actionButtons = List.of(addButton, reloadButton, clearButton);
        setLayout(null);
        add(fileFilter);
        for (JButton button : actionButtons) {
            add(button);
        }
        overflowButton.setMargin(JBUI.insets(2, 6));
        int buttonSize = Math.max(JBUI.scale(28), overflowButton.getPreferredSize().height);
        overflowButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        overflowButton.setToolTipText("API actions");
        overflowButton.getAccessibleContext().setAccessibleName("API actions");
        overflowButton.addActionListener(event -> {
            overflowMenu = createOverflowMenu();
            overflowMenu.show(overflowButton, 0, overflowButton.getHeight());
        });
        overflowButton.setVisible(false);
        add(overflowButton);
    }

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        int availableWidth = Math.max(0, getWidth() - insets.left - insets.right);
        DisplayMode nextMode = chooseDisplayMode(availableWidth);
        if (displayMode != nextMode) {
            if (overflowMenu != null) {
                overflowMenu.setVisible(false);
            }
            fileFilter.hidePopup();
            displayMode = nextMode;
        }
        boolean compact = displayMode == DisplayMode.COMPACT;
        fileFilter.setVisible(!compact);
        int visibleActions = visibleActionCount(displayMode);
        for (int index = 0; index < actionButtons.size(); index++) {
            actionButtons.get(index).setVisible(index < visibleActions);
        }
        overflowButton.setVisible(displayMode != DisplayMode.FULL);
        overflowButton.setText(compact ? "Action ▾" : "▾");
        String description = compact ? "Files and API actions" : "API actions";
        overflowButton.setToolTipText(description);
        overflowButton.getAccessibleContext().setAccessibleName(description);

        int height = Math.max(0, getHeight() - insets.top - insets.bottom);
        int x = insets.left;
        if (!compact) {
            int fileWidth = availableWidth - actionWidth(displayMode);
            fileFilter.setBounds(x, insets.top, fileWidth, height);
            x += fileWidth;
        }
        for (Component component : getComponents()) {
            if (component != fileFilter && component.isVisible()) {
                if (x > insets.left) {
                    x += JBUI.scale(4);
                }
                int width = compact ? availableWidth : Math.min(component.getPreferredSize().width, availableWidth);
                component.setBounds(x, insets.top, width, height);
                x += width;
            }
        }
    }

    private DisplayMode chooseDisplayMode(int width) {
        for (DisplayMode mode : DisplayMode.values()) {
            if (mode == DisplayMode.COMPACT || width >= JBUI.scale(96) + actionWidth(mode)) {
                return mode;
            }
        }
        return DisplayMode.COMPACT;
    }

    private int visibleActionCount(DisplayMode mode) {
        return switch (mode) {
            case FULL -> 3;
            case RELOAD_AND_CLEAR_MENU -> 1;
            case ACTIONS_MENU, COMPACT -> 0;
        };
    }

    private int actionWidth(DisplayMode mode) {
        int width = 0;
        for (int index = 0; index < visibleActionCount(mode); index++) {
            width += actionButtons.get(index).getPreferredSize().width + JBUI.scale(4);
        }
        if (mode != DisplayMode.FULL) {
            width += overflowButton.getPreferredSize().width + JBUI.scale(4);
        }
        return width;
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        int height = Math.max(fileFilter.getPreferredSize().height, overflowButton.getPreferredSize().height);
        for (JButton button : actionButtons) {
            height = Math.max(height, button.getPreferredSize().height);
        }
        return new Dimension(JBUI.scale(96) + actionWidth(DisplayMode.FULL) + insets.left + insets.right,
                height + insets.top + insets.bottom);
    }

    @Override
    public Dimension getMinimumSize() {
        Insets insets = getInsets();
        return new Dimension(overflowButton.getPreferredSize().width + insets.left + insets.right,
                getPreferredSize().height);
    }

    JPopupMenu createOverflowMenu() {
        JPopupMenu menu = new JPopupMenu();
        if (displayMode == DisplayMode.COMPACT) {
            menu.add(createFilesMenu());
        }
        for (int index = visibleActionCount(displayMode); index < actionButtons.size(); index++) {
            JButton button = actionButtons.get(index);
            JMenuItem item = new JMenuItem(button.getToolTipText(), button.getIcon());
            item.setEnabled(button.isEnabled());
            item.addActionListener(event -> button.doClick());
            menu.add(item);
        }
        return menu;
    }

    private JMenu createFilesMenu() {
        JMenu files = new JMenu("Files");
        ButtonGroup group = new ButtonGroup();
        for (int index = 0; index < fileFilter.getItemCount(); index++) {
            Object option = fileFilter.getItemAt(index);
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(option.toString(),
                    index == fileFilter.getSelectedIndex());
            item.addActionListener(event -> fileFilter.setSelectedItem(option));
            group.add(item);
            files.add(item);
        }
        return files;
    }
}
