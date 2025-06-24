import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import com.google.gson.Gson;

public class ExchangeFiat {
    private static final String API_KEY = "0f9f332aeda528d4aed132f6";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/pair/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    // Enum para organizar os pares de moedas
    private enum ParesMoedas {
        BRL_USD("BRL", "USD"),
        USD_BRL("USD", "BRL"),
        BRL_EUR("BRL", "EUR"),
        EUR_BRL("EUR", "BRL");

        final String from;
        final String to;

        ParesMoedas(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            exibirMenu();

            if (!scanner.hasNextInt()) {
                System.out.println("Digite apenas números.");
                scanner.next();
                continue;
            }

            int option = scanner.nextInt();
            if (option == 5) break;

            if (option < 1 || option > 5) {
                System.out.println("Opção inválida!");
                continue;
            }

            System.out.print("Digite o valor: ");
            if (!scanner.hasNextDouble()) {
                System.out.println("Digite um valor numérico válido.");
                scanner.next();
                continue;
            }
            double valor = scanner.nextDouble();

            try {
                ParesMoedas conversao = obterConversao(option);
                double resultado = converterMoeda(conversao.from, conversao.to, valor);
                System.out.printf("\n%.2f %s = %.2f %s\n", valor, conversao.from, resultado, conversao.to);

            } catch (Exception e) {
                System.out.println("Erro na conversão: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Programa encerrado.");
    }

    // Exibe o menu de opções
    private static void exibirMenu() {
        System.out.println("\n*** CONVERSOR DE MOEDAS FIAT ***");
        System.out.println("1. BRL para USD");
        System.out.println("2. USD para BRL");
        System.out.println("3. BRL para EUR");
        System.out.println("4. EUR para BRL");
        System.out.println("5. Sair");
        System.out.print("\nEscolha uma opção: ");
    }

    // Mapeia a opção do menu para o par de moedas correspondente
    private static ParesMoedas obterConversao(int option) {
        return switch (option) {
            case 1 -> ParesMoedas.BRL_USD;
            case 2 -> ParesMoedas.USD_BRL;
            case 3 -> ParesMoedas.BRL_EUR;
            case 4 -> ParesMoedas.EUR_BRL;
            default -> throw new IllegalArgumentException("Opção inválida.");
        };
    }

    // Realiza a conversão utilizando a API
    private static double converterMoeda(String from, String to, double valor) throws Exception {
        String url = BASE_URL + from + "/" + to;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("API indisponível. Código: " + response.statusCode());
        }

        RespostaConversao resposta = gson.fromJson(response.body(), RespostaConversao.class);
        return valor * resposta.conversion_rate();
    }

    // Record para mapear a resposta JSON da API — compatível com Gson 2.10.1+
    private record RespostaConversao(
            String result,
            String documentation,
            String terms_of_use,
            long time_last_update_unix,
            String time_last_update_utc,
            long time_next_update_unix,
            String time_next_update_utc,
            String base_code,
            String target_code,
            double conversion_rate
    ) {}
}
