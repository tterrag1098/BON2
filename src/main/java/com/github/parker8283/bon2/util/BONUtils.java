package com.github.parker8283.bon2.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class BONUtils {
    /**
     * Installs a listener to receive notification when the text of any
     * {@code JTextComponent} is changed. Internally, it installs a
     * {@link DocumentListener} on the text component's {@link Document},
     * and a {@link PropertyChangeListener} on the text component to detect
     * if the {@code Document} itself is replaced.
     *
     * @param text any text component, such as a {@link JTextField}
     *        or {@link JTextArea}
     * @param changeListener a listener to receieve {@link ChangeEvent}s
     *        when the text is changed; the source object for the events
     *        will be the text component
     * @throws NullPointerException if either parameter is null
     */
    public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0, lastNotifiedChange = 0;

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document)e.getOldValue();
            Document d2 = (Document)e.getNewValue();
            if (d1 != null) d1.removeDocumentListener(dl);
            if (d2 != null) d2.addDocumentListener(dl);
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) d.addDocumentListener(dl);
    }

    /**
     * @author Ilias Tsagklis
     *         <p>
     *         From <a href= "http://examples.javacodegeeks.com/core-java/util/zip/extract-zip-file-with-subdirectories/" > this site.</a>
     *
     * @param zip
     *          - The zip file to extract
     *
     * @return The folder extracted to
     */
    public static File extractZip(File zip) {
      String zipPath = zip.getParent();
      final File temp = new File(zipPath);
      temp.mkdir();

      ZipFile zipFile = null;

      try {
        zipFile = new ZipFile(zip);

        // get an enumeration of the ZIP file entries
        Enumeration<? extends ZipEntry> e = zipFile.entries();

        while (e.hasMoreElements()) {
          ZipEntry entry = e.nextElement();

          File destinationPath = new File(zipPath, entry.getName());

          // create parent directories
          destinationPath.getParentFile().mkdirs();

          // if the entry is a file extract it
          if (entry.isDirectory()) {
            continue;
          } else {
            System.out.println("Extracting file: " + destinationPath);

            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

            int b;
            byte buffer[] = new byte[1024];

            FileOutputStream fos = new FileOutputStream(destinationPath);

            BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

            while ((b = bis.read(buffer, 0, 1024)) != -1) {
              bos.write(buffer, 0, b);
            }

            bos.close();
            bis.close();
          }
        }
      } catch (IOException e) {
        System.err.print("Error opening zip file:");
        e.printStackTrace();
      } finally {
        try {
          if (zipFile != null) {
            zipFile.close();
          }
        } catch (IOException e) {
            System.err.println("Error while closing zip file:");
            e.printStackTrace();
        }
      }

      return temp;
    }
}
