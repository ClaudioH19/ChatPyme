package labsd;

public class Clock extends Thread{

    @Override
    public void run(){
        Basededatos db = new Basededatos();
        while(true){

            try{
                Thread.sleep(5000);
                //HiloDeCliente.groups= db.getallgroups();

            }catch (Exception e){
                System.out.println(e);
            }

        }


    }
}
