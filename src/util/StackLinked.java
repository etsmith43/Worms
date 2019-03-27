package util;

public class StackLinked<E>
{
   private Node<E> top;
   private int size;

   public StackLinked() 
   {
      top = null; 
      size = 0;
   } 
  
   public boolean isEmpty() 
   {
      return size == 0;
   }  

   public int size()
   {
      return size;
   }

   public void popAll() 
   {
      top = null;
      size = 0;
   }  

   public E peek()
   {
	   if (isEmpty()) return null;
	   return top.getItem();
   }  

   public void push(E item)
   {
      Node<E> node = new Node<E>(item);
      node.setNext(top);
      top = node;
      size++;
   }  

   public E pop()
   {
	   if (isEmpty()) return null;

         E temp = top.getItem();
         top = top.getNext();
         size--;
         return temp;
   } 
}
