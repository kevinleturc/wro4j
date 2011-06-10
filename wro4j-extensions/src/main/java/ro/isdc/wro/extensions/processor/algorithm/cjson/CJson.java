/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.algorithm.cjson;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * The underlying implementation use the cjson project: {@link http://stevehanov.ca/blog/index.php?id=104}.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public class CJson {
  private static final Logger LOG = LoggerFactory.getLogger(CJson.class);


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final InputStream scriptStream = getScriptAsStream();
      return RhinoScriptBuilder.newClientSideAwareChain().addJSON().evaluateChain(
          scriptStream, "script.js");
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed reading javascript script.js", ex);
    } catch (final Exception e) {
      LOG.error("Processing error:" + e.getMessage(), e);
      throw new WroRuntimeException("Processing error", e);
    }
  }


  /**
   * @return stream of the less.js script.
   */
  protected InputStream getScriptAsStream() {
    return getClass().getResourceAsStream("cjson.js");
  }

  public String unpack(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("json.unpack");
    try {
      final String execute = "CJSON.parse('" + WroUtil.toJSMultiLineString(data) + "');";
      final Object result = builder.evaluate(execute, "unpack");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }

  /**
   * @param data css content to process.
   * @return processed css content.
   */
  public String pack(final String data) {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("initContext");
    final RhinoScriptBuilder builder = initScriptBuilder();
    stopWatch.stop();

    stopWatch.start("cjson.pack");
    try {
      final String execute = "CJSON.stringify(JSON.parse(" + WroUtil.toJSMultiLineString(data) + "));";
      final Object result = builder.evaluate(execute, "pack");
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    } finally {
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
}
