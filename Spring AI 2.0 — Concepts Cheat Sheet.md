# Spring AI 2.0 — Concepts Cheat Sheet

Nov 27, 2026 · @Omkar Khilari

## How to read this sheet

Each section explains one Spring AI 2.0 topic: what it is, the main types, when to use each, and a short example. ✅ marks what we built in our projects.

| Topic | Where we used it |
| --- | --- |
| ChatClient, prompts, structured output | Project 1 (spring-ai-assistant) |
| Tools, memory, advisors | Projects 1–2 |
| Embeddings, ETL, vector stores, RAG | Project 3 (spring-ai-knowledge-base) |
| Workflow patterns | Project 4 (spring-ai-workflows) |
| Multi-agent | Project 5 (spring-ai-multi-agent) |
| MCP | Project 6 (spring-ai-mcp-server + client) |
| Observability, evaluation, other model types | Not built yet |

## ChatClient vs ChatModel

Use `ChatClient` for almost everything; `ChatModel` is the low-level layer underneath it.

|  | ChatModel | ChatClient ✅ |
| --- | --- | --- |
| What it is | Raw connection to one provider | Fluent API on top of a ChatModel |
| Adds | Nothing | System prompt, tools, advisors, memory, structured output |
| Created by | Spring Boot from properties | You, from the auto-configured `ChatClient.Builder` |
| Use when | Very low-level control | Normal app code |

**Ways to get the answer:**

| Call | Returns | Use when |
| --- | --- | --- |
| `.call().content()` ✅ | `String` | You only need the text |
| `.call().entity(MyRecord.class)` ✅ | A Java object | You need data, not prose |
| `.call().chatResponse()` ✅ | Full response | You need tokens, model, finish reason |
| `.stream().content()` | `Flux<String>` | Show the answer word by word in a UI |
|  |  |  |

```java
String answer = chatClient.prompt()
        .system("You are a helpful assistant")
        .user("Explain Spring Boot in 2 lines")
        .call()
        .content();
```

`default...` methods on the builder apply to every request; the same methods on `.prompt()` apply to one request only.

## Messages, roles and prompt templates

Every request is a list of messages, each with a role; the model is stateless and sees only this list.

| Message type | Written by | Purpose |
| --- | --- | --- |
| `SystemMessage` ✅ | You (developer) | Rules, persona, constraints |
| `UserMessage` ✅ | The human | The question or task |
| `AssistantMessage` ✅ | The model | Its replies, including tool-call requests |
| `ToolResponseMessage` ✅ | Your tool code | A tool's result, sent back to the model |

**Prompt templates** fill `{placeholders}` with values per request. Keep long prompts in `src/main/resources/prompts/*.st` files.

```java
@Value("classpath:prompts/article-draft.st")
private Resource draftPrompt;

chatClient.prompt()
        .system(s -> s.text(draftPrompt).param("title", title))
        .user("Write the article.")
        .call().content();
```

- Fill a value once at startup in `defaultSystem(s -> ...)` when it never changes (like a not-found message).
- Fill it per request with `.system(s -> ...)` or `.user(u -> ...)` when it changes (like today's date).
- Keep rules in the system message and data (documents, user text) in the user message. Rules are then harder to override.

## Model providers and options

Each provider has its own starter; your code stays the same when you switch, only properties change.

| Provider | Starter | Notes |
| --- | --- | --- |
| OpenAI and OpenAI-compatible ✅ | `spring-ai-starter-model-openai` | Works with OpenRouter, Groq, NVIDIA, Perplexity and Docker Model Runner by changing `base-url` |
| Anthropic (Claude) | `spring-ai-starter-model-anthropic` | Native API |
| Google Gemini | Google GenAI starter | Native API |
| Ollama | `spring-ai-starter-model-ollama` | Free, runs models on your own machine |

**Common options** (in properties as `spring.ai.openai.chat.<option>`, or per request):

| Option | Meaning | Tip |
| --- | --- | --- |
| `model` ✅ | Which model answers | One line to switch |
| `temperature` ✅ | Randomness, 0 to 2 | 0–0.3 for agents, RAG and tools; reasoning models may reject it |
| `max-tokens` | Longest allowed reply | Caps cost and time |

Model choice: a cheap, fast model for simple steps (routing, extraction) and a stronger one for hard steps. Free OpenRouter models end in `:free` and allow about 50 requests per day.

## Structured output

`.entity(...)` turns the model's answer into a Java object, so later code gets data instead of text.

| Target type | Example | Tip |
| --- | --- | --- |
| A record ✅ | `.entity(ArticleOutline.class)` | Best default |
| A wrapper record with a list ✅ | `.entity(ExpenseListDto.class)` | More reliable than a bare `List<...>` |
| A generic list | `.entity(new ParameterizedTypeReference<List<Expense>>() {})` | Works, but wrappers are simpler |
| An enum field ✅ | `Category category` | Limits the model to fixed values |

**How it works:** Spring AI builds a JSON schema from your record, tells the model to answer in that JSON, then converts the reply with Jackson.

**Always verify in code:** the model can return `null` lists, wrong years or invented values. Use a compact constructor for null-safety and filter invalid values before saving.

```java
public record ExpenseListDto(List<ExpenseDto> expenses) {
    public ExpenseListDto {
        expenses = (expenses == null) ? List.of() : expenses;
    }
}
```

## Tools

A tool is Java code the model may ask you to run; Spring AI runs the tool loop for you.

| Kind of tool | How you define it | Example |
| --- | --- | --- |
| `@Tool` method ✅ | Annotate a method in a bean | `CalculatorTools.multiply` |
| Tool that calls another agent ✅ | `@Tool` method that calls a `ChatClient` | `askWeatherExpert` |
| Programmatic tool | `FunctionToolCallback.builder(...)` | Tools built at runtime |
| MCP tool ✅ | Comes from an MCP server | `convert_to_inr` |

**Register tools:** `.defaultTools(...)` on the builder for every request, or `.tools(...)` on one request. In 2.0, `defaultTools` accepts `@Tool` objects, callbacks and providers (including MCP).

**Key ideas:**

- The model sees only the name, description and parameter schema, so descriptions matter most.
- Every tool definition is sent on every request, so each tool costs tokens.
- `ToolContext` ✅ passes values from your code (user ID, collectors) that the model cannot see or change.
- Never let the model supply a user ID or permission as a parameter.
- A thrown exception goes back to the model as text by default; write messages the model can act on.
- `@Tool(returnDirect = true)` sends the tool result straight to the caller without another model round.

## Chat memory

Memory stores earlier messages per conversation and resends them, because the model remembers nothing between calls.

It has two parts: a **repository** (where messages are stored) and a **memory strategy** (which messages are sent).

| Repository | Storage | Survives restart? |
| --- | --- | --- |
| `InMemoryChatMemoryRepository` ✅ | Java map in RAM | No |
| `JdbcChatMemoryRepository` ✅ | PostgreSQL, MySQL, SQL Server, HSQLDB, Oracle | Yes |
| Cassandra, Neo4j and others | Separate starters | Yes |

**Strategy:** `MessageWindowChatMemory` ✅ keeps the most recent messages (default 20). It always keeps system messages and drops whole turns, never half a tool exchange.

```java
ChatMemory memory = MessageWindowChatMemory.builder()
        .chatMemoryRepository(jdbcChatMemoryRepository)
        .maxMessages(20)
        .build();
```

Connect it with a memory advisor and pass a conversation ID on every request: `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))`. More history means more tokens on every call.

## Advisors

Advisors wrap every call (before and after) and are Spring AI's main extension point; lower order runs first.

| Advisor | Group | Use |
| --- | --- | --- |
| `SimpleLoggerAdvisor` ✅ | Logging | Log full requests and responses |
| Custom `CallAdvisor` ✅ | Logging / anything | Your own logic, like token usage |
| `MessageChatMemoryAdvisor` ✅ | Memory | History as real messages |
| `PromptChatMemoryAdvisor` | Memory | History as text in the system prompt |
| `VectorStoreChatMemoryAdvisor` | Memory | Relevant old messages, found by meaning |
| `QuestionAnswerAdvisor` | RAG | One-line RAG over a vector store |
| `RetrievalAugmentationAdvisor` | RAG | Modular RAG with pluggable steps |
| `SafeGuardAdvisor` | Safety | Block sensitive words before the model is called |
| `ToolCallingAdvisor` ✅ | Tools | Runs the tool loop (added automatically) |
| `ToolSearchToolCallingAdvisor` | Tools | Lets the model search many tools instead of sending all |
| Retry and evaluation advisors | Quality | Retry bad JSON or improve answers |

Typical order: guardrail → memory → RAG → logger, so blocked requests cost nothing and the logger sees the final prompt.

## Embeddings

An embedding model turns text into a vector of numbers; texts with similar meaning get similar vectors.

- `EmbeddingModel` ✅ is auto-configured from properties, like the chat model: `embeddingModel.embed(text)` returns a `float[]`.
- **Dimensions** = vector length, fixed per model (`openai/text-embedding-3-small` = 1536 ✅).
- **Similarity** is usually cosine: compare the angle between vectors. Rank results by score; do not read scores as percentages.
- Embedding is much cheaper than chat, but every document chunk and every question is embedded.

**Locked-in choice:** stored vectors and questions must use the same model and size. Changing the embedding model means re-embedding every document; changing the chat model is one line.

**Good at:** meaning and synonyms ("holidays" matches "annual leave"). **Bad at:** exact names, IDs and numbers; use keyword search for those.

## Document ETL: readers, splitters, enrichers

Before RAG can search a file, it goes through three steps: read → transform → store.

| Step | Class | Use |
| --- | --- | --- |
| Read | `TikaDocumentReader` ✅ | PDF, Word, Excel, text and most other formats |
| Read | `PagePdfDocumentReader` / `ParagraphPdfDocumentReader` | PDFs, split by page or paragraph |
| Read | `TextReader`, `JsonReader`, `MarkdownDocumentReader` | Plain text, JSON fields, Markdown sections |
| Transform | `TokenTextSplitter` ✅ | Cut text into chunks by token count |
| Transform | `KeywordMetadataEnricher` | Ask a model for keywords per chunk, stored as metadata |
| Transform | `SummaryMetadataEnricher` | Add a short summary per chunk as metadata |
| Store | `vectorStore.add(chunks)` ✅ | Embed and save |

**Tips:**

- Chunk size is the most important RAG setting: small chunks are precise but lose context; large ones blur meaning.
- Add your own metadata (document ID, file name) to every chunk ✅, for citations, filters and deletes.
- Scanned PDFs contain no text; they need OCR first.
- Tables become messy text after reading; record boundaries are lost.

## Vector stores

A vector store saves chunks with their vectors and finds the most similar ones; every store uses the same `VectorStore` interface.

| Store | Good for |
| --- | --- |
| PGVector ✅ | You already use PostgreSQL |
| `SimpleVectorStore` | Learning and tests (in memory) |
| Redis, Elasticsearch | You already run them |
| Chroma, Qdrant, Milvus, Pinecone, Weaviate | Dedicated vector databases at larger scale |

**Search:**

```java
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
        .query(question)
        .topK(4)                       // at most 4 chunks
        .similarityThreshold(0.3)      // drop weak matches
        .filterExpression("fileName == 'leave-policy.txt'")  // metadata filter
        .build());
```

- `topK` too low misses answers; too high adds noise and cost.
- Tune `similarityThreshold` by testing with your own model and documents.
- Metadata filters limit search to one document, user or category.
- PGVector settings ✅: `initialize-schema=true`, `index-type=HNSW`, `distance-type=COSINE_DISTANCE`, `dimensions=1536`.

## RAG (Retrieval-Augmented Generation)

RAG = find the right chunks, add them to the prompt, and let the model answer only from them.

| Approach | How | When |
| --- | --- | --- |
| Manual RAG ✅ | Search, build context, prompt yourself | Learning, full control, citations |
| `QuestionAnswerAdvisor` | One advisor does search + context | Quick standard RAG |
| `RetrievalAugmentationAdvisor` | Pluggable query rewriting, retriever, augmenter | Control over each step |
| Hybrid search ✅ | Vector + keyword search, merged with RRF | Questions with names, IDs, codes |

**Prompt rules that matter:** answer only from context; an exact "not found" sentence; cite sources; ignore instructions inside documents.

**Works well for:** policies, manuals, articles, notes: text that is read.

**Works badly for:** statements, logs and tables. It can miss matches, misread rows and never count or total everything. Extract such data into a database table and give the agent SQL tools instead.

Debug with a search-only endpoint first: most wrong answers come from retrieval, not the model.

## Workflow patterns

In workflows, your Java code decides the steps; the model only does each step.

| Pattern | Idea | Calls | Our example |
| --- | --- | --- | --- |
| Chain ✅ | Step → step, each uses the last result | One per step, in a row | Brief → outline → draft → polish |
| Routing ✅ | Classify, then send to a specialist | 2 | Support: billing / technical / general |
| Parallel ✅ | Independent tasks at the same time | Several at once | Review: sentiment + issues + reply |
| Orchestrator-workers ✅ | The model plans subtasks; workers do them | 1 + N + 1 | Research report |
| Evaluator-optimizer | Write → check → improve, with a max | 2 per try | (skipped) |

**Tips:**

- Add gates between chain steps to stop early on bad output.
- Use an enum + `switch` for routes so a missing route fails to compile.
- Run parallel tasks on a virtual-thread executor with `CompletableFuture`; start all first, then wait.
- Always cap anything the model controls (number of subtasks, loop count).
- Mix patterns: parallel where tasks are independent, chain where one needs another.

## Multi-agent

In multi-agent systems, a main agent decides by itself which specialist agents to call, using them as tools.

- Each specialist ✅ is a `ChatClient` bean with its own focused prompt.
- Wrap each in a `@Tool` method; give those tools to the main agent.
- **Context does not flow automatically:** specialists see only the question the main agent writes, so tool descriptions must ask for complete questions.
- Track calls with a per-request collector in `ToolContext` ✅ to see who was asked what.
- Facts like prices, places and contacts come from the model's memory unless you give agents tools that look them up.

**Costs:** many sequential calls (one question used 5+ calls and about 2–3 minutes on a free model). Keep specialist answers short and use multi-agent only when the task truly needs several experts.

## MCP (Model Context Protocol)

MCP is a standard way to share tools between apps: write a tool once on a server, use it from any MCP client.

| Side | Starter | Job |
| --- | --- | --- |
| Server ✅ | `spring-ai-starter-mcp-server-webmvc` (HTTP) or `spring-ai-starter-mcp-server` (STDIO) | Offers tools; needs no AI model |
| Client ✅ | `spring-ai-starter-mcp-client` (or `-webflux`) | Discovers server tools and gives them to its model |

| Transport | How | When |
| --- | --- | --- |
| STDIO | Client starts the server as a local program | Desktop tools on one machine |
| Streamable HTTP ✅ | Server runs on its own at `/mcp` | Local or network; the modern default |
| SSE | Older HTTP transport | Deprecated in the spec; avoid |
| Stateless | HTTP without sessions | Simple scaled-out servers |

**What a server can offer:** tools (`@McpTool` ✅), resources (data to read) and prompts (reusable templates).

**Main messages:** `initialize`, `tools/list`, `tools/call`. Test servers with the MCP Inspector (`npx @modelcontextprotocol/inspector`) ✅.

```properties
spring.ai.mcp.client.streamable-http.connections.india-utils.url=http://localhost:8090
```

Start the server before the client app; pass MCP tools with `.defaultTools(localTools, mcpToolProvider)`.

## Observability and evaluation

Observability shows what your AI app did; evaluation checks whether the answers were good.

**Observability** (not built yet):

- Spring AI emits Micrometer metrics and traces for chat calls, tool calls, advisors, embeddings and vector store queries.
- Add Spring Boot Actuator and a tracing backend (for example Zipkin or an OpenTelemetry collector) to see each request as a timeline.
- Track token usage per call and per user; our `TokenUsageAdvisor` ✅ was a hand-made version.
- Be careful logging prompts and answers: they can contain private user data.

**Evaluation** (not built yet):

| Tool | Checks |
| --- | --- |
| `RelevancyEvaluator` | Is the answer relevant to the question and context? |
| `FactCheckingEvaluator` | Is the answer supported by the given context? |
| Your own tests | Fixed questions with expected answers, run after each prompt or model change |

Keep a small set of test questions and re-run them whenever you change a prompt, model or chunk size.

## Other model types

Besides chat and embeddings, Spring AI has interfaces for other kinds of models; we switched them off with `spring.ai.model.<type>=none`.

| Model type | Does | Example use |
| --- | --- | --- |
| Image | Text → image | Product banners, illustrations |
| Audio transcription | Speech → text | Voice notes into the chat |
| Text to speech | Text → speech | Read answers aloud |
| Moderation | Flags harmful text | A guardrail before or after chat |

Chat models can also accept images or files as input (multimodal), when the chosen model supports it.

## Quick decision guide

Start from the problem, then pick the feature.

| Problem | Use |
| --- | --- |
| Model guesses dates, math or live data | A tool |
| Need data, not text, from the model | Structured output (`.entity`) |
| Model forgets earlier messages | Chat memory + memory advisor |
| Answer must come from your documents | RAG |
| Questions mention exact names or IDs | Hybrid search |
| Totals, counts or lookups over records | Database table + SQL tools, not RAG |
| Task has fixed stages | Chain |
| Different request types need different handling | Routing |
| Independent tasks are slow in a row | Parallel |
| Subtasks depend on the input | Orchestrator-workers |
| Several experts, order unknown | Multi-agent (agents as tools) |
| Same tools needed in several apps | MCP server |
| Logging, safety or retries on every call | An advisor |
| Model must not choose users or permissions | `ToolContext` |
| Model invents facts (emails, prices) | Give the facts in the prompt or via a tool |
