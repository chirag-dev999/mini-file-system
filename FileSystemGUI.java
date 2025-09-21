import javax.swing.*;
import java.awt.*;          
import java.awt.event.*;    
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;       


class FileSystemGUI {
    enum NodeType { FILE, DIR }

   static class Node {
    String name;
    NodeType type;
    StringBuilder content;
    Node parent;
    List<Node> children;

    Node(String name, NodeType type) {
        this.name = name;
        this.type = type;
        if (type == NodeType.FILE) {
            content = new StringBuilder();
        } else { 
            children = new ArrayList<>();
        }
    }
}

    private Node root = new Node("", NodeType.DIR);
    private Node cwd = root;

    // ----- Core Operations -----
    private Node find(String name) {
        for (Node child : cwd.children) {
            if (child.name.equals(name)) return child;
        }
        return null;
    }

    void mkdir(String name) { if (find(name)==null) cwd.children.add(new Node(name, NodeType.DIR)); }
    void touch(String name) { if (find(name)==null) cwd.children.add(new Node(name, NodeType.FILE)); }
    String ls() {
        if (cwd.children.isEmpty()) return "Directory is empty.";
        StringBuilder sb = new StringBuilder();
        for (Node c : cwd.children) sb.append(c.name).append(c.type==NodeType.DIR?"/  ":"  ");
        return sb.toString();
    }
    void cd(String name) {
        if ("..".equals(name)) { if (cwd.parent!=null) cwd = cwd.parent; return; }
        Node d = find(name);
        if (d!=null && d.type==NodeType.DIR) cwd = d;
    }
    String pwd() {
        List<String> path = new ArrayList<>();
        Node cur = cwd;
        while (cur!=null) { if (!cur.name.isEmpty()) path.add(cur.name); cur=cur.parent; }
        Collections.reverse(path);
        return "/" + String.join("/", path);
    }
    void write(String name, String data) { Node f=find(name); if (f!=null && f.type==NodeType.FILE) { f.content.setLength(0); f.content.append(data);} }
    String read(String name) { Node f=find(name); return (f!=null&&f.type==NodeType.FILE)? f.content.toString() : "File not found."; }
    void rm(String name) { Node f=find(name); if (f!=null&&f.type==NodeType.FILE) cwd.children.remove(f); }
    void rmdir(String name) { Node d=find(name); if (d!=null&&d.type==NodeType.DIR&&d.children.isEmpty()) cwd.children.remove(d); }
    String tree() {
        return draw(root,0);
    }
    private String draw(Node n, int depth) {
        StringBuilder sb=new StringBuilder("    ".repeat(depth)+ (n.name.isEmpty()?"/":n.name)+(n.type==NodeType.DIR?"/":"")+"\n");
        for(Node c:n.children) sb.append(draw(c,depth+1));
        return sb.toString();
    }

    // ----- GUI -----
    public void launch() {
        JFrame frame = new JFrame("Mini File System");
        frame.setSize(600,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea output = new JTextArea();
        output.setEditable(false);
        JScrollPane scroll = new JScrollPane(output);

        JTextField input = new JTextField();
        input.addActionListener(e -> {
            String line = input.getText().trim();
            input.setText("");
            if (line.isEmpty()) return;

            String[] parts = line.split(" ",3);
            String cmd = parts[0];
            String arg1 = parts.length>1?parts[1]:null;
            String arg2 = parts.length>2?parts[2]:null;
            String res="";

            switch(cmd) {
                case "mkdir" -> { if(arg1!=null) mkdir(arg1); }
                case "touch" -> { if(arg1!=null) touch(arg1); }
                case "ls" -> res=ls();
                case "cd" -> { if(arg1!=null) cd(arg1); }
                case "pwd" -> res=pwd();
                case "write" -> { if(arg1!=null && arg2!=null) write(arg1,arg2); }
                case "read" -> { if(arg1!=null) res=read(arg1); }
                case "rm" -> { if(arg1!=null) rm(arg1); }
                case "rmdir" -> { if(arg1!=null) rmdir(arg1); }
                case "tree" -> res=tree();
                case "exit" -> System.exit(0);
                default -> res="Unknown command: "+cmd;
            }
            output.append("filesystem:"+pwd()+"$ "+line+"\n");
            if(!res.isEmpty()) output.append(res+"\n");
        });

        frame.setLayout(new BorderLayout());
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileSystemGUI().launch());
    }
}
