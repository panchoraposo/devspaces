import { getNodeAutoInstrumentations } from '@opentelemetry/auto-instrumentations-node';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { NodeSDK } from '@opentelemetry/sdk-node';
import {
  BasicTracerProvider,
  ConsoleSpanExporter,
  SimpleSpanProcessor,
} from '@opentelemetry/sdk-trace-node';
import { Resource } from '@opentelemetry/resources';
import { SERVICE_NAME } from '@opentelemetry/semantic-conventions';

class Tracer {
  constructor() {
    this.sdk = null;

    // Exportador de trazas a OpenTelemetry Collector
    this.exporter = new OTLPTraceExporter({ url: 'http://dev-collector.observability.svc.cluster.local:4317/v1/traces' });

    this.provider = new BasicTracerProvider({
      resource: new Resource({
        [SERVICE_NAME]: 'DEMO-APP',
      }),
    });
  }

  init() {
    try {
      // Exportar spans a la consola (útil para depuración)
      this.provider.addSpanProcessor(
        new SimpleSpanProcessor(new ConsoleSpanExporter())
      );

      // Exportar spans al colector OpenTelemetry
      this.provider.addSpanProcessor(new SimpleSpanProcessor(this.exporter));
      this.provider.register();

      this.sdk = new NodeSDK({
        traceExporter: this.exporter,
        instrumentations: [
          getNodeAutoInstrumentations({
            // Deshabilitar fs para evitar trazas innecesarias
            '@opentelemetry/instrumentation-fs': { enabled: false },
          }),
        ],
      });

      this.sdk.start();

      console.info('El tracer ha sido inicializado');
    } catch (e) {
      console.error('Error al inicializar el tracer', e);
    }
  }
}

export default new Tracer();