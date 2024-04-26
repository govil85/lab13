package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {
    private static class TreeNode {
        private String name;
        private TreeNode parent;
        private ArrayList<TreeNode> children;

        TreeNode(String name) {
            this.name = name;
            children = new ArrayList<>();
        }

        String getName() {
            return name;
        }

        void addChild(TreeNode childNode) {
            childNode.parent = this; // Set childNode's parent to this node
            children.add(childNode); // Add childNode to this node's children list
        }

        TreeNode getNodeWithName(String targetName) {
            // Check if this node's name matches the target name
            if (this.name.equals(targetName))
                return this;

            // Recursively search all children for the node with the target name
            for (TreeNode child : children) {
                TreeNode foundNode = child.getNodeWithName(targetName);
                if (foundNode != null)
                    return foundNode;
            }

            // Not found anywhere
            return null;
        }

        ArrayList<TreeNode> collectAncestorsToList() {
            ArrayList<TreeNode> ancestors = new ArrayList<>();
            TreeNode current = this; // Start from the current node

            // Traverse up the tree until reaching the root
            while (current != null) {
                ancestors.add(current); // Add current node to ancestors list
                current = current.parent; // Move up to the parent node
            }

            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + name + "\n";
            indent += "  ";
            for (TreeNode childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode root;

    public FamilyTree() throws IOException, TreeException {
        // User chooses input file. This block doesn't need any work.
        FileNameExtensionFilter filter =
                new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists())
            dirf = new File(".");
        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
            System.exit(1);
        File treeFile = chooser.getSelectedFile();

        // Parse the input file. Create a FileReader that reads treeFile. Create a BufferedReader
        // that reads from the FileReader.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null)
            addLine(line);
        br.close();
        fr.close();
    }

    private void addLine(String line) throws TreeException {
        // Extract parent and array of children
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0)
            throw new TreeException("Invalid line format: " + line);
        String parent = line.substring(0, colonIndex);
        String childrenString = line.substring(colonIndex + 1);
        String[] childrenArray = childrenString.split(",");

        // Find or create parent node
        TreeNode parentNode;
        if (root == null)
            parentNode = root = new TreeNode(parent);
        else {
            parentNode = root.getNodeWithName(parent);
            if (parentNode == null)
                throw new TreeException("Parent node '" + parent + "' not found.");
        }

        // Add child nodes to parentNode
        for (String child : childrenArray) {
            TreeNode childNode = new TreeNode(child.trim());
            parentNode.addChild(childNode);
        }
    }

    TreeNode getMostRecentCommonAncestor(String name1, String name2) throws TreeException {
        // Get nodes for input names
        TreeNode node1 = root.getNodeWithName(name1);
        if (node1 == null)
            throw new TreeException("Node with name '" + name1 + "' not found.");
        TreeNode node2 = root.getNodeWithName(name2);
        if (node2 == null)
            throw new TreeException("Node with name '" + name2 + "' not found.");

        // Get ancestors of node1 and node2
        ArrayList<TreeNode> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode> ancestorsOf2 = node2.collectAncestorsToList();

        // Check members of ancestorsOf1 in order until you find a node that is also an ancestor of 2
        for (TreeNode n1 : ancestorsOf1)
            if (ancestorsOf2.contains(n1))
                return n1;

        // No common ancestor found
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();
            System.out.println("Tree:\n" + tree + "\n**************\n");
            TreeNode ancestor = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");
            System.out.println("Most recent common ancestor of Bilbo and Frodo is " + ancestor.getName());
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
}
