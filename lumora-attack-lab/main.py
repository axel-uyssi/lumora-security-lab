from config import MAX_THREADS

from attacks.brute_force import BruteForceAttack

from core.engine import RequestEngine
from core.queue_manager import QueueManager
from core.workers import Worker

from utils.metrics import Metrics
from utils.report import ReportGenerator

TARGET_EMAIL = "admin@test.com"

WORDLIST = "passwords.txt"

def main():

    queue_manager = QueueManager()

    metrics = Metrics()

    engine = RequestEngine()

    worker = Worker(
        queue_manager,
        engine,
        metrics
    )

    worker.start(threads=MAX_THREADS)

    attack = BruteForceAttack(queue_manager)

    attack.load(
        TARGET_EMAIL,
        WORDLIST
    )

    queue_manager.queue.join()

    ReportGenerator.generate(metrics)

if __name__ == "__main__":
    main()