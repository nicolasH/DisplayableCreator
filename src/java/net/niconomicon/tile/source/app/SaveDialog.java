/**
 * 
 */
package net.niconomicon.tile.source.app;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author Nicolas Hoibian
 *
 */
public class SaveDialog {

	protected JTextField outputFileName;
	protected JTextField where;
	protected JButton browseOutput;

	JTextArea description;
	JTextField author;
	JTextField title;

	public SaveDialog(){
		
	}
	
	public void init(){
		GridBagConstraints c ;
		int x,y ;
		x = y = 0;
		JPanel option = new JPanel(new GridBagLayout());

		
		title = new JTextField("", 20);
		description = new JTextArea("", 5, 30);
		author = new JTextField(System.getProperty("user.name"), 20);

		
		
		
		
		
		
		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x;
		c.anchor = c.LINE_END;
		option.add(new JLabel("Title :"), c);

		// //////////////

		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x;
		c.anchor = c.LINE_END;
		option.add(new JLabel("Save as :"), c);
		
		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x;
		c.anchor = c.LINE_END;
		option.add(new JLabel("In directory :"),c);
		
		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		option.add(title, c);
		
		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = c.LINE_START;

		option.add(outputFileName, c);

		
		c = new GridBagConstraints();
		c.gridy = y;
		c.gridx = x;
		c.anchor = c.LINE_START;
		option.add(where, c);
		
		c = new GridBagConstraints();
		c.gridy = y++;
		c.gridx = x+1;
		option.add(browseOutput, c);
		

	}
}
