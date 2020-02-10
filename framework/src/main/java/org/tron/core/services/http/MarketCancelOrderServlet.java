package org.tron.core.services.http;

import com.alibaba.fastjson.JSONObject;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.contract.AssetIssueContractOuterClass.TransferAssetContract;
import org.tron.protos.contract.MarketContract.MarketCancelOrderContract;
import org.tron.protos.contract.MarketContract.MarketSellAssetContract;


@Component
@Slf4j(topic = "API")
public class MarketCancelOrderServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String contract = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(contract);

      boolean visible = Util.getVisiblePost(contract);
      MarketCancelOrderContract.Builder build = MarketCancelOrderContract.newBuilder();
      JsonFormat.merge(contract, build, visible);

      Transaction tx = wallet
          .createTransactionCapsule(build.build(), ContractType.MarketCancelOrderContract)
          .getInstance();

      JSONObject jsonObject = JSONObject.parseObject(contract);
      tx = Util.setTransactionPermissionId(jsonObject, tx);
      tx = Util.setTransactionExtraData(jsonObject, tx);

      response.getWriter().println(Util.printCreateTransaction(tx, visible));
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}