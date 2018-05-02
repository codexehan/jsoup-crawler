package com.bing.start;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerUtils;

import com.bing.crawler.CrawlerNoDB;
public class App implements Job
{
	private CrawlerNoDB crawlerNoDB;
	public App(){
		this.crawlerNoDB = new CrawlerNoDB();
	}
    public static void main( String[] args )
    {
    	new App().crawlerNoDB.start();
    	/*try {
            // 创建一个Scheduler
            SchedulerFactory schedFact = 
            new org.quartz.impl.StdSchedulerFactory();
            Scheduler scheduler = schedFact.getScheduler();
            
            // 创建一个JobDetail，指明name，groupname，以及具体的Job类名，
            //该Job负责定义需要执行任务
            JobDetail job = JobBuilder.newJob(App.class)
                    .withIdentity("myJob")
                    .build();
            Trigger trigger=TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")  
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 12,18,22 * * ?"))//每天12 1  
                    .startNow().build();   
            job.getJobDataMap().put("type", "FULL");

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
            System.out.println("start crawle..");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		
		crawlerNoDB.start();
 
	}
}
