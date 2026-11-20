# Survivor Auth

Mod de login por senha para servidores **NeoForge 1.21.1** em `online-mode=false`, com uma **tela de login pós-apocalíptica** (visual de sobrevivência) em vez de comandos no chat.

- **Servidor:** o mod é obrigatório.
- **Cliente:** opcional. Com o mod, o jogador ganha a tela. Sem o mod, ele entra normalmente e usa `/register` e `/login` no chat.

## Compilar

Precisa de JDK 21.

```
./gradlew build
```

No Windows: `gradlew.bat build`. O `.jar` sai em `build/libs/survivorauth-1.0.0.jar`.
Para testar no jogo: `./gradlew runClient` (cliente) e `./gradlew runServer` (servidor).

## Instalar

1. Coloque o `.jar` na pasta `mods` do servidor.
2. Quem quiser a tela coloca o mesmo `.jar` na pasta `mods` do cliente.
3. Inicie o servidor. Os arquivos abaixo são criados sozinhos.

| Arquivo | O que é |
|---|---|
| `world/serverconfig/survivorauth-server.toml` | Tempo limite, tamanho mínimo da senha, tentativas, bloqueio |
| `world/survivorauth/accounts.json` | Contas (só o hash PBKDF2-SHA256 + sal, nunca a senha) |
| `config/survivorauth-client.toml` (cliente) | Cores e efeitos da tela |

## Como personalizar o visual

### 1. Cores e efeitos
Edite `config/survivorauth-client.toml` no cliente: cor de destaque, texto, painel, escurecimento do fundo, linhas de varredura, vinheta e pulsar do título.

### 2. Fundo e logo (sem mexer em código)
Crie um resource pack e coloque as imagens nestes caminhos:

```
assets/survivorauth/textures/gui/background.png   <- fundo (de preferência 16:9, ex. 1920x1080)
assets/survivorauth/textures/gui/logo.png         <- logo no lugar do título "SOBREVIVENTE"
```

- O fundo cobre a tela inteira sem distorcer; use `backgroundDarkness` na config para escurecer.
- Sem as imagens, o mod usa um fundo desenhado por código (céu sujo + névoa).

### 3. Textos
Todos os textos da tela ficam em `assets/survivorauth/lang/pt_br.json` (e `en_us.json`). Um resource pack pode sobrescrever qualquer chave (`survivorauth.title`, `survivorauth.button.login`...).

Mensagens que o **servidor** envia (chat/kick) ficam em `server/Msg.java`.

### 4. Layout e formas
Tudo que é desenhado está em `client/LoginScreen.java` (painel, faixa de perigo, colchetes de canto, botões, posições).

## Comandos

| Comando | Quem | Função |
|---|---|---|
| `/register <senha> <senha>` | Jogador | Registra o nome |
| `/login <senha>` | Jogador | Entra |
| `/changepassword <atual> <nova>` | Jogador logado | Troca a senha |
| `/sauth unregister <nome>` | OP nível 3 | Apaga a conta (se estiver online, volta a registrar) |
| `/sauth logout <jogador>` | OP nível 3 | Obriga a se identificar de novo |

Pela tela, a senha pode ter espaços e símbolos. Pelos comandos de chat, a senha não pode ter espaços.

## Enquanto o jogador não se identifica

Fica congelado no lugar (pode olhar em volta), não toma dano, não fala, só executa `/login` e `/register`, não quebra/coloca blocos, não usa itens nem ataca. Se estourar o tempo (90 s por padrão) é expulso. Senhas erradas são contadas por IP: ao estourar o limite, o IP é expulso e bloqueado por alguns minutos.

## Limitações conhecidas

- Em `online-mode=false` a conexão **não é criptografada**: a senha viaja como digitada, igual a qualquer comando de chat. Para proteger contra quem consegue ler o tráfego, o próximo passo seria um desafio-resposta (o cliente envia um hash em vez da senha).
- Bloqueios por IP ficam em memória (zeram ao reiniciar o servidor).
- Jogadores atrás do mesmo IP compartilham o contador de senhas erradas.
- Não há "lembrar sessão por IP" nem migração para conta original (online) como no DirectAuth.
- Mensagens do servidor para jogadores sem o mod estão só em português.
