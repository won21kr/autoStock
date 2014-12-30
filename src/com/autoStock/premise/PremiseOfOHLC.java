/**
 * 
 */
package com.autoStock.premise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.autoStock.Co;
import com.autoStock.backtest.BacktestUtils;
import com.autoStock.backtest.BacktestUtils.LookDirection;
import com.autoStock.database.DatabaseQuery;
import com.autoStock.database.DatabaseDefinitions.BasicQueries;
import com.autoStock.database.DatabaseDefinitions.QueryArg;
import com.autoStock.database.DatabaseDefinitions.QueryArgs;
import com.autoStock.exchange.request.RequestHistoricalData;
import com.autoStock.exchange.request.base.RequestHolder;
import com.autoStock.exchange.request.listener.RequestHistoricalDataListener;
import com.autoStock.exchange.results.ExResultHistoricalData.ExResultSetHistoricalData;
import com.autoStock.generated.basicDefinitions.TableDefinitions.DbStockHistoricalPrice;
import com.autoStock.signal.extras.EncogFrame;
import com.autoStock.signal.extras.EncogFrame.FrameType;
import com.autoStock.signal.extras.EncogFrameSupport;
import com.autoStock.signal.extras.EncogFrameSupport.EncogFrameSource;
import com.autoStock.signal.extras.EncogSubframe;
import com.autoStock.tools.DateTools;
import com.autoStock.tools.QuoteSliceTools;
import com.autoStock.trading.platform.ib.definitions.HistoricalDataDefinitions.Resolution;
import com.autoStock.trading.types.HistoricalData;
import com.autoStock.types.Exchange;
import com.autoStock.types.QuoteSlice;
import com.autoStock.types.Symbol;

/**
 * @author Kevin
 * So hacky but meh
 *
 */
public class PremiseOfOHLC extends PremiseBase implements EncogFrameSource {
	public Exchange exchange;
	public Symbol symbol;
	public Date dateStart;
	public int days;
	public Resolution resolution;
	public ArrayList<QuoteSlice> listOfQuotes;
	
	public PremiseOfOHLC(Exchange exchange, Symbol symbol, Date dateStart, Resolution resolution, int days) {
		this.exchange = exchange;
		this.symbol = symbol;
		this.dateStart = dateStart;
		this.resolution = resolution;
		this.days = days;
	}

	@Override
	public void run(){
		if (resolution == Resolution.day){
			populateForDaily();
		}else if (resolution == Resolution.min_15 || resolution == Resolution.min_30 || resolution == Resolution.hour){
			populateForMinute();
		}else{
			throw new IllegalArgumentException("Can't handle resolution: " + resolution);
		}
	}
	
	private void populateForMinute(){
		ArrayList<HistoricalData> list = BacktestUtils.getHistoricalDataListForDates(new HistoricalData(exchange, symbol, dateStart, null, resolution), LookDirection.backward, days);
		ArrayList<DbStockHistoricalPrice> results = new ArrayList<DbStockHistoricalPrice>();
		ArrayList<QuoteSlice> listOfOHLC = new ArrayList<QuoteSlice>();
		
		for (HistoricalData historicalData : list){
			results.addAll((ArrayList<DbStockHistoricalPrice>) new DatabaseQuery().getQueryResults(BasicQueries.basic_historical_price_range, new QueryArg(QueryArgs.symbol, historicalData.symbol.symbolName), new QueryArg(QueryArgs.startDate, DateTools.getSqlDate(historicalData.startDate)), new QueryArg(QueryArgs.endDate, DateTools.getSqlDate(historicalData.endDate))));			
		}

		int index = 0;
		QuoteSlice quoteSlice = new QuoteSlice(symbol);
		quoteSlice.priceOpen = results.get(0).priceOpen;
		quoteSlice.dateTime = results.get(0).dateTime;
		quoteSlice.priceHigh = Double.MIN_VALUE;
		quoteSlice.priceLow = Double.MAX_VALUE;
		
		for (DbStockHistoricalPrice price : results){
			if (index % resolution.asMinutes() == 0 && index != 0){
				listOfOHLC.add(quoteSlice);
				quoteSlice = new QuoteSlice(symbol);
				quoteSlice.dateTime = price.dateTime;
				quoteSlice.priceOpen = price.priceOpen;
				quoteSlice.priceHigh = Double.MIN_VALUE;
				quoteSlice.priceLow = Double.MAX_VALUE;
			}
			
			quoteSlice.priceHigh = Math.max(quoteSlice.priceHigh, price.priceHigh);
			quoteSlice.priceLow = Math.min(quoteSlice.priceLow, price.priceLow);
			quoteSlice.priceClose = price.priceClose;
			
			index++;
		}
		
		listOfQuotes = listOfOHLC;
	}
	
	private void populateForDaily(){
		ArrayList<HistoricalData> list = BacktestUtils.getHistoricalDataListForDates(new HistoricalData(exchange, symbol, dateStart, null, resolution), LookDirection.backward, days);
		ArrayList<QuoteSlice> listOfOHLC = new ArrayList<QuoteSlice>();
		
		for (HistoricalData data : list){
			ArrayList<DbStockHistoricalPrice> results = (ArrayList<DbStockHistoricalPrice>) new DatabaseQuery().getQueryResults(BasicQueries.basic_historical_price_range, new QueryArg(QueryArgs.symbol, data.symbol.symbolName), new QueryArg(QueryArgs.startDate, DateTools.getSqlDate(data.startDate)), new QueryArg(QueryArgs.endDate, DateTools.getSqlDate(data.endDate)));
			
			QuoteSlice quote = new QuoteSlice(symbol);
			
			quote.dateTime = results.get(0).dateTime;
			quote.priceOpen = results.get(0).priceOpen;
			quote.priceClose = results.get(results.size()-1).priceClose;
			quote.priceHigh = Double.MIN_VALUE;
			quote.priceLow = Double.MAX_VALUE;
			
			for (DbStockHistoricalPrice price : results){
				quote.priceHigh = Math.max(quote.priceHigh, price.priceHigh);
				quote.priceLow = Math.min(quote.priceLow, price.priceLow);
			}
			
			listOfOHLC.add(quote);
		}
		
		listOfQuotes = listOfOHLC;
	}

	@Override
	public EncogFrame asEncogFrame() {
		EncogFrame encogFrame = new EncogFrame("OHLC for: " + symbol.symbolName + ", " + DateTools.getPrettyDate(dateStart) + ", " + resolution.name(), FrameType.raw);
		ArrayList<Double> values = new ArrayList<Double>();
		
		for (QuoteSlice quote : listOfQuotes){
			values.add(quote.priceOpen);
			values.add(quote.priceHigh);
			values.add(quote.priceLow);
			values.add(quote.priceClose);
		}
		
		encogFrame.addSubframe(new EncogSubframe(values, FrameType.raw));
		return encogFrame;
	}
}