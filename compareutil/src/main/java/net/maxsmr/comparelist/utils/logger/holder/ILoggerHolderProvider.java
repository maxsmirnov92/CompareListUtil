package net.maxsmr.comparelist.utils.logger.holder;


public interface ILoggerHolderProvider<H extends BaseLoggerHolder> {

     H provideHolder();
}
