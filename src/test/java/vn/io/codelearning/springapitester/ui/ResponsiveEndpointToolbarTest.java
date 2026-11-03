package vn.io.codelearning.springapitester.ui;

import com.intellij.util.ui.JBUI;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponsiveEndpointToolbarTest {

    @Test
    public void testShrinksFromRightToLeftAndRestoresControlsAndSelectedFile() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<String> files = new JComboBox<>(new String[]{"Files", "UserController.java"});
            files.setSelectedIndex(1);
            JButton add = actionButton("New Collection");
            JButton reload = actionButton("Reload APIs");
            JButton clear = actionButton("Clear all saved data…");
            ResponsiveEndpointToolbar toolbar = new ResponsiveEndpointToolbar(files, add, reload, clear);

            int[] widths = {340, 180, 145, 110, 145, 180, 340};
            int[] counts = {4, 3, 2, 1, 2, 3, 4};
            for (int index = 0; index < widths.length; index++) {
                layout(toolbar, widths[index]);
                Assert.assertEquals(counts[index], visibleControlCount(toolbar));
                Assert.assertEquals(counts[index] > 1, files.isVisible());
                Assert.assertEquals(counts[index] >= 3, add.isVisible());
                Assert.assertEquals(counts[index] == 4, reload.isVisible());
                Assert.assertEquals(counts[index] == 4, clear.isVisible());
                Assert.assertEquals("UserController.java", files.getSelectedItem());
                assertControlsFit(toolbar);
                JButton dropdown = (JButton) toolbar.getComponent(toolbar.getComponentCount() - 1);
                Assert.assertEquals(counts[index] == 1 ? "Action ▾" : "▾", dropdown.getText());
                if (counts[index] == 1) {
                    Assert.assertEquals(0, dropdown.getX());
                    Assert.assertEquals(toolbar.getWidth(), dropdown.getWidth());
                    Assert.assertEquals(toolbar.getHeight(), dropdown.getHeight());
                }
            }
        });
    }

    @Test
    public void testEveryWidthKeepsVisibleControlsInsideToolbarWithoutOverlap() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<String> files = new JComboBox<>(new String[]{"Files", "VeryLongControllerName".repeat(20)});
            files.setSelectedIndex(1);
            ResponsiveEndpointToolbar toolbar = new ResponsiveEndpointToolbar(files,
                    actionButton("New Collection"), actionButton("Reload APIs"), actionButton("Clear all saved data…"));
            toolbar.setBorder(JBUI.Borders.empty(3, 5));
            Set<Integer> displayedCounts = new HashSet<>();
            int previousCount = 1;
            for (int width = 40; width <= 480; width++) {
                layout(toolbar, width);
                int count = visibleControlCount(toolbar);
                Assert.assertTrue("Controls must return as space increases", count >= previousCount);
                displayedCounts.add(count);
                previousCount = count;
                assertControlsFit(toolbar);
                if (files.isVisible()) {
                    Assert.assertTrue("Files must remain readable", files.getWidth() >= JBUI.scale(96));
                } else {
                    Component dropdown = toolbar.getComponent(toolbar.getComponentCount() - 1);
                    Insets insets = toolbar.getInsets();
                    Assert.assertEquals(insets.left, dropdown.getX());
                    Assert.assertEquals(toolbar.getWidth() - insets.left - insets.right, dropdown.getWidth());
                }
            }
            Assert.assertEquals(Set.of(1, 2, 3, 4), displayedCounts);
            Assert.assertTrue("Long filenames must not inflate the preferred toolbar width",
                    toolbar.getPreferredSize().width <= JBUI.scale(240));
        });
    }

    @Test
    public void testOverflowMenusContainOnlyTheGroupedActionsAndRunOriginalHandlers() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<String> files = new JComboBox<>(new String[]{"Files", "OldController.java"});
            AtomicInteger additions = new AtomicInteger();
            AtomicInteger reloads = new AtomicInteger();
            AtomicInteger clears = new AtomicInteger();
            JButton add = actionButton("New Collection");
            JButton reload = actionButton("Reload APIs");
            JButton clear = actionButton("Clear all saved data…");
            add.addActionListener(event -> additions.incrementAndGet());
            reload.addActionListener(event -> reloads.incrementAndGet());
            clear.addActionListener(event -> clears.incrementAndGet());
            ResponsiveEndpointToolbar toolbar = new ResponsiveEndpointToolbar(files, add, reload, clear);

            layout(toolbar, 180);
            JPopupMenu pairedMenu = toolbar.createOverflowMenu();
            assertMenuLabels(pairedMenu, "Reload APIs", "Clear all saved data…");
            ((JMenuItem) pairedMenu.getComponent(0)).doClick();
            ((JMenuItem) pairedMenu.getComponent(1)).doClick();
            Assert.assertEquals(0, additions.get());
            Assert.assertEquals(1, reloads.get());
            Assert.assertEquals(1, clears.get());

            layout(toolbar, 145);
            assertMenuLabels(toolbar.createOverflowMenu(), "New Collection", "Reload APIs", "Clear all saved data…");
            ((JMenuItem) toolbar.createOverflowMenu().getComponent(0)).doClick();
            Assert.assertEquals(1, additions.get());
            clear.setEnabled(false);
            Assert.assertFalse(((JMenuItem) toolbar.createOverflowMenu().getComponent(2)).isEnabled());

            layout(toolbar, 110);
            files.setModel(new DefaultComboBoxModel<>(new String[]{"Files", "NewController.java"}));
            JPopupMenu compactMenu = toolbar.createOverflowMenu();
            assertMenuLabels(compactMenu, "Files", "New Collection", "Reload APIs", "Clear all saved data…");
            JMenu fileMenu = (JMenu) compactMenu.getComponent(0);
            Assert.assertEquals("NewController.java", fileMenu.getItem(1).getText());
            fileMenu.getItem(1).doClick();
            Assert.assertEquals("NewController.java", files.getSelectedItem());
            JMenu refreshedFiles = (JMenu) toolbar.createOverflowMenu().getComponent(0);
            Assert.assertTrue(refreshedFiles.getItem(1).isSelected());
            layout(toolbar, 340);
            Assert.assertEquals("NewController.java", files.getSelectedItem());
        });
    }

    private JButton actionButton(String description) {
        JButton button = new JButton();
        button.setToolTipText(description);
        button.setPreferredSize(JBUI.size(28, 28));
        return button;
    }

    private void layout(ResponsiveEndpointToolbar toolbar, int width) {
        toolbar.setSize(JBUI.scale(width), toolbar.getPreferredSize().height);
        toolbar.doLayout();
    }

    private int visibleControlCount(ResponsiveEndpointToolbar toolbar) {
        int count = 0;
        for (Component child : toolbar.getComponents()) {
            if (child.isVisible()) {
                count++;
            }
        }
        return count;
    }

    private void assertControlsFit(ResponsiveEndpointToolbar toolbar) {
        Insets insets = toolbar.getInsets();
        int rightEdge = insets.left;
        for (Component child : toolbar.getComponents()) {
            if (child.isVisible()) {
                Assert.assertTrue(child.getWidth() > 0);
                Assert.assertTrue("Visible controls must not overlap", child.getX() >= rightEdge);
                rightEdge = child.getX() + child.getWidth();
                Assert.assertTrue("Visible controls must not be clipped", rightEdge <= toolbar.getWidth() - insets.right);
            }
        }
    }

    private void assertMenuLabels(JPopupMenu menu, String... labels) {
        Assert.assertEquals(labels.length, menu.getComponentCount());
        for (int index = 0; index < labels.length; index++) {
            Assert.assertEquals(labels[index], ((JMenuItem) menu.getComponent(index)).getText());
        }
    }
}
