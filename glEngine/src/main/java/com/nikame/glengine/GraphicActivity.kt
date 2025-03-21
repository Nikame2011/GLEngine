package com.nikame.glengine

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nikame.glengine.gObjects.AbstractGObject
import com.nikame.glengine.gObjects.FLAG_COLLISIBLE
import com.nikame.glengine.gObjects.FLAG_MOVABLE
import com.nikame.glengine.graphics.MainRender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Date

//todo для реализации движка нужно дополнительно создать:
// - класс с набором графических объектов, набор внутри класса должен отрисовываться как единое целое
// и использовать заданные анимации, а также реализовывать управление объектом извне
// - класс, реализующий стандартные анимации - повороты, смещения итд
// - класс, отвечающий за камеру и её поведение - получение управления извне или следование за указанным объектом,
// логику изменения параметров - с плавной задержкой или чёткое следование
// - логику контроля фреймов (имеются в виду не графические фреймы а фреймы обновления данных, т.к. это разные вещи)
// определить метод с задержками для поддержания заданного уровня и балансировщик для компенсации времени между кадрами,
// например. если кард раз в 1 тик, а произошёл через 1.3 тика, то координаты нужно считать как старые+скорость*1,3 или что-то типа того

/***
 * This is a blank activity designed to start the resource initialization process.
 *
 * Inherit from this class in your launcher activity.
 * Prepare a set of drawables by overwriting buildGObjects. Call super in onCreate after your code.
 * If you need to know the progress of the initialization, call setInitProgressListener in onCreate before call super
 */


abstract class GraphicActivity : AppCompatActivity() {
    var onProgressChanged: ((progress: Int, status: String) -> Unit)? = null

    lateinit var glSurfaceView: GLSurfaceView
    var isRenderAdded = false
    lateinit var gObjects: ArrayList<AbstractGObject>
    lateinit var collisible: ArrayList<AbstractGObject>
    lateinit var movable: ArrayList<AbstractGObject>

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (notSupportES2()) {
            finish()
            return
        }

        MainRender.defRender = lifecycleScope.async(Dispatchers.Default) {
            gObjects = buildGObjects()
            collisible = gObjects.filter { it.flags and FLAG_COLLISIBLE > 0 } as ArrayList
            movable = gObjects.filter { it.flags and FLAG_MOVABLE > 0 } as ArrayList
            return@async MainRender(buildParams(), gObjects)
        }
        onProgressChanged?.let { it(0, "start") }


        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)

        lifecycleScope.launch {
            glSurfaceView.setRenderer(
                MainRender.getRender()
            )
            setContentView(glSurfaceView)
            isRenderAdded = true
            onProgressChanged?.let { it(100, "finish") }
            launch(Dispatchers.Default) {
                control_date = Date()
                while (true) {

                    //player=new Space_ship( "0",300+rand.nextInt(200),0,0,0,0);
                    //player.init(getContext(),(byte) 0,300+rand.nextInt(200),0,0,0,0);

                    /*  Space_station s= new Space_station("0",100000,0,0,0,0);
                        //s.init(getContext(), (byte) 0,100000,0,0,0,0);
                        stations.add(s);

                        long target_y=-10000-rand.nextInt(1000);
                        long target_x= (long) Math.sqrt(12000*12000-target_y*target_y);

                        s= new Space_station( "0",100000,target_x,target_y,0,0);
                        //s.init(getContext(), (byte) 0,100000,target_x,target_y,0,0);
                        stations.add(s);*/
                    //target=stations.size()-1;

                    /* for(int h=0; h<dh;h+=dh/10){
                            for (int m=0; m<1+rand.nextInt(3);m++){
                                Meteorid m0=new Meteorid();
                                m0.init(getContext(),(byte) 0,50+rand.nextInt(50),rand.nextInt(dw)+offset_x,h+rand.nextInt(dh/80)+offset_y,0,0);
                                met.add(m0);
                            }
                        }*/
                    game()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (isRenderAdded)
            glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isRenderAdded)
            glSurfaceView.onResume()
    }

    abstract fun buildParams(): MainRender.Companion.Params

    abstract fun buildGObjects(): ArrayList<AbstractGObject>

    private fun notSupportES2(): Boolean {
        return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.reqGlEsVersion < 0x20000
    }


    /*     int dop_inc=1;
   private void control_first() { // пауза и контроль количества кадров

        d = new Date();

        if ((float) (d.getTime() - control_date.getTime()) / 250>=1 &tick>0) {
            frames= (byte) (tick*1000/(float) (d.getTime() - control_date.getTime()));
            control_date = new Date();
            tick=0;
//            dop_inc=increment * frames>60?60/((increment * frames) % 60):0;
            increment=Math.max(increment * frames / 60, 1);



        }
        else{
            tick++;
        }

        //  else{
        //         byte fr= (byte) (tick*1000/(float) (d.getTime() - control_date.getTime()));
        //        increment=increment*fr/60>1 ? increment*fr/60 : 1;
        //   }

        try {
            if(dop_inc!=0){
                if(tick%dop_inc==0)
                    gameThread.sleep(increment*2);
                else
                    gameThread.sleep(increment);}
            else
                gameThread.sleep(increment);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/
    /*if (cadres-frames>cadres/6)
                if(increment>1)
                    increment--;
                else if(cadres==60){
                    cadres=30;
                    speed_coef=2;
                }
            if (cadres-frames<-cadres/6 )
                increment++;*/
    /*   private void control_second() { // пауза и контроль количества кадров

        d = new Date();

        if ((float) (d.getTime() - control_date.getTime()) / 50>=1 &tick>0) {
            frames= (byte) (tick*1000/(float) (d.getTime() - control_date.getTime()));
            control_date = new Date();
            tick=0;
            //dop_inc=increment * frames>60?60/((increment * frames) % 60):0;
            increment=Math.max(increment * frames / 60, 100);


        }
        else{
            tick++;
        }

        //  else{
        //         byte fr= (byte) (tick*1000/(float) (d.getTime() - control_date.getTime()));
        //        increment=increment*fr/60>1 ? increment*fr/60 : 1;
        //   }

        try {
            //if(dop_inc!=0){
           //     if(tick%dop_inc==0)
             //       gameThread.sleep(increment*2);
            //    else
               //     gameThread.sleep(increment);}
           // else
                gameThread.sleep(increment/1000,increment%1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/


    private val framesTarget: Byte = 30
    private val frameDelay = (1000 / framesTarget).toShort().toInt()
    private var controlTick = 0
    private var delayDebt = 0

    private var increment = 0f
    private var frames: Byte = 0
    private var control_date: Date? = null
    private var d: Date? = null

    fun game() {


        /*           j=new JSONObject();
                try {
                   // j.put("stars",stars);
                    j.put("meteorid",met.clone());

                   // ArrayList<> nn=j.get("meteorid");
                    n.clear();
                    n= (ArrayList<Space_meteorite>) j.get("meteorid");
                 //   Class c =j.get("meteorid").getClass();
                 //   Class cc =j.get("meteorid").getClass();
                  / *  JSONArray jj=j.getJSONArray("meteorid");

                    Gson gson = new GsonBuilder().create();
                    n.clear();

                    for (int i=0;0<jj.length();i++) {
                        n.add(gson.fromJson(jj.get(i).toString(),Space_meteorite.class ));
                    }*/

        //Gson gson = new GsonBuilder().create();
        //ArrayList<Space_meteorite> n= gson.fromJson(j.getJSONObject("meteorid").toString(),Space_meteorite.class );
        /*     } catch (JSONException e) {
                    e.printStackTrace();
                }
*/
        val startDate = Date()
        onFrameChanged()

        for (gObject in movable) {
            gObject.update(1f)
        }

        val collisibleTargets = ArrayList<AbstractGObject> (collisible)

        for (obj in collisible) {
            collisibleTargets.remove(obj)
            for (obj2 in collisibleTargets) {
                if (obj.position.add(obj.center).sub(obj2.position.add(obj2.center))
                        .length() < obj.baseCollisionRadius + obj2.baseCollisionRadius
                ) {
                    val speed1 = obj.speed
                    val speed2 = obj2.speed
                    obj.speed = speed2
                    obj2.speed = speed1
                }
            }
        }

        val finishDate = Date()
        framesControl(startDate, finishDate)
    }

    abstract fun onFrameChanged()

    private fun framesControl(
        startDate: Date,
        finishDate: Date
    ) { // пауза и контроль количества кадров
        controlTick++
        val delay = Math.max(finishDate.time - startDate.time + delayDebt, 0).toInt()
        if (delay < frameDelay) {
            try {
                Thread.sleep((frameDelay - delay).toLong())
                increment = (frameDelay - delay).toFloat()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } else {
            increment = 0f
            delayDebt = delay - frameDelay
        }
        d = Date()
        if (d!!.time - control_date!!.time >= 500) {
            frames = (controlTick * 2).toByte()
            if (frames > framesTarget || delayDebt > 500) delayDebt = 0
            controlTick = 0
            control_date = Date()

//            Log.e("frame", "on count ${frames} ${delayDebt}")
        }
    }
}

