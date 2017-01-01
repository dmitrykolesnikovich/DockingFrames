package tutorial.support;

import bibliothek.gui.dock.DefaultDockable;

import javax.swing.*;

public class TextDockable extends DefaultDockable {
  private JTextArea area;

  public TextDockable(String title) {
    setTitleText(title);

    area = new JTextArea();
    area.setLineWrap(false);
    area.setTabSize(2);
    area.setEditable(false);

    add(new JScrollPane(area));
  }

  public void appendText(String text) {
    area.append(text);
  }

  public String getText() {
    return area.getText();
  }

  public void setText(String text) {
    area.setText(text);
  }
}
